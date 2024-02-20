package com.firisbe.service.Implementation;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.error.CustomerNotFoundException;
import com.firisbe.error.PaymentFailedException;
import com.firisbe.error.TransferNotFoundException;
import com.firisbe.model.Account;
import com.firisbe.model.Customer;
import com.firisbe.model.DTO.request.CustomerPaymentRequest;
import com.firisbe.model.DTO.response.PaymentResponse;
import com.firisbe.model.Transfer;
import com.firisbe.repository.jpa.TransferRepository;
import com.firisbe.service.Interface.TransferServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TransferServiceImplementation implements TransferServiceInterface {
    private final TransferRepository repo;
    private final AccountServiceImplementation accountService;
    private final CustomerServiceImplementation customerService;
    private final KafkaTemplate<String, String> kafkaTemplate;


    @Override
    public GenericResponse<String> sendPaymentMessageToKafka(CustomerPaymentRequest request, String token) {
        try {
            Account senderAccount = customerService.findCustomerToToken(token).getAccount();
            Account receiveAccount = customerService.findCustomerByMail(request.receiveMail()).getAccount();
            if (senderAccount != null && receiveAccount != null) {
                String message = senderAccount.getId() + "/" + receiveAccount.getId() + "/" + request.amount();

                kafkaTemplate.send("paymentProcess", message);

                kafkaTemplate.send("paymentLog", "Payment transaction received successfully: " +
                        senderAccount.getCustomer().getName() + " sent " + request.amount()
                        + " amount of money to account with credit card number " + receiveAccount.getCustomer().getName() + " .");

                return new GenericResponse<>("Payment request received successfully!", true);

            } else {
                throw new PaymentFailedException();
            }
        } catch (Exception e) {
            throw new PaymentFailedException(e);
        }

    }

    @Override
    @KafkaListener(
            topics = {"${kafka.topic.payment_process}"},
            groupId = "${kafka.groupId}"
    )
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = {PaymentFailedException.class})
    public void processPaymentMessageFromKafka(String request) {
        try {

            String[] parts = request.split("/");

            long senderAccountId = Long.parseLong(parts[0]);
            long receiverAccountId = Long.parseLong(parts[1]);
            BigDecimal amount = new BigDecimal(parts[2]);


            Account senderAccount = accountService.findAccountById(senderAccountId);
            Account receiveAccount = accountService.findAccountById(receiverAccountId);
            Customer senderCustomer = senderAccount.getCustomer();
            Customer receiveCustomer = receiveAccount.getCustomer();

            if (senderAccount.getBalance().compareTo(amount) > 0) {
                senderAccount.setBalance(
                        senderAccount.getBalance().subtract(amount)
                );
                receiveAccount.setBalance(
                        receiveAccount.getBalance().add(amount)
                );
                accountService.accountSave(senderAccount);
                accountService.accountSave(receiveAccount);

                Transfer transfer = Transfer.builder()
                        .amount(amount)
                        .sender(senderCustomer)
                        .receiver(receiveCustomer)
                        .timestamp(LocalDateTime.now())
                        .build();
                /* Bu ifadelere gerek var mı ? */
//                senderCustomer.getSentTransfers().add(transfer);
//                receiveCustomer.getReceivedTransfers().add(transfer);
                repo.save(transfer);

                kafkaTemplate.send("payment-log", "Payment transaction received recorded: " +
                        senderAccount.getCustomer().getName() + " sent " + amount
                        + " amount of money to account with credit card number " + receiveAccount.getCustomer().getName() + " .");
            } else {
                throw new PaymentFailedException();
            }
        } catch (PaymentFailedException e) {
            throw new PaymentFailedException(e);
        } catch (Exception e) {
            throw new RuntimeException("The cause of the error is unknown. Possible reason: ", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = {PaymentFailedException.class})
    public GenericResponse<PaymentResponse> readPaymentForCustomer(String token, Long id) {
        try {
            Transfer transfer = repo.findById(id).orElseThrow(TransferNotFoundException::new);
            Customer customer = customerService.findCustomerToToken(token);
            if (transfer.getSender().getId().equals(customer.getId())) {
                PaymentResponse response = new PaymentResponse(
                        transfer.getSender(),
                        transfer.getReceiver(),
                        transfer.getAmount(),
                        transfer.getTimestamp()
                );
                return new GenericResponse<>(response, true);
            } else {
                throw new CustomerNotFoundException();
            }
        } catch (CustomerNotFoundException e) {
            throw new CustomerNotFoundException(e);
        } catch (TransferNotFoundException e) {
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllReceivedPaymentForCustomer(String token) {
        return listTransfer(token, "received");
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllSentPaymentForCustomer(String token) {
        return listTransfer(token, "sent");
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllPaymentForCustomer(String token) {
        return listTransfer(token, "all");
    }

    @Override
    public GenericResponse<PaymentResponse> readPaymentForAdmin(Long id) {
        try {
            Transfer transfer = repo.findById(id).orElseThrow(TransferNotFoundException::new);
            return new GenericResponse<>(
                    new PaymentResponse(
                            transfer.getSender(),
                            transfer.getReceiver(),
                            transfer.getAmount(),
                            transfer.getTimestamp()
                    ), true
            );
        } catch (TransferNotFoundException e) {
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllPaymentForAdmin() {
        try {
            List<Transfer> transferList = repo.findAll();
            List<PaymentResponse> response = transferList.stream()
                    .map(transfer -> new PaymentResponse(
                            transfer.getSender(),
                            transfer.getReceiver(),
                            transfer.getAmount(),
                            transfer.getTimestamp()
                    )).toList();
            return new GenericResponse<>(response, true);
        } catch (TransferNotFoundException e) {
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     *     Bu method Customer 'ın transfer işlemlerini ne şekilde yapacağını belirtmek için oluşturulmuştur. 3 tane fonksiyonda benzer
     *     işlemler yapıldığı için yapıların sadeleştirilmesi için oluşturulmuştur.
     */
    private GenericResponse<List<PaymentResponse>> listTransfer(String token, String value) {
        try {
            Customer customer = customerService.findCustomerToToken(token);
            List<Transfer> transferList = new ArrayList<>();
            switch (value) {
                case "sent" -> transferList.addAll(customer.getSentTransfers());
                case "received" -> transferList.addAll(customer.getReceivedTransfers());
                case "all" -> {
                    transferList.addAll(customer.getReceivedTransfers());
                    transferList.addAll(customer.getSentTransfers());
                }
                default -> {
                }
            }
            if (!transferList.isEmpty()) {
                List<PaymentResponse> response = transferList.stream().map(
                        transfer -> new PaymentResponse(
                                transfer.getSender(),
                                transfer.getReceiver(),
                                transfer.getAmount(),
                                transfer.getTimestamp()
                        )
                ).toList();
                return new GenericResponse<>(response, true);
            } else {
                throw new TransferNotFoundException();
            }
        } catch (TransferNotFoundException e) {
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
