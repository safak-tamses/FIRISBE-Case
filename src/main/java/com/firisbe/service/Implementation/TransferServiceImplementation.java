package com.firisbe.service.Implementation;

import com.firisbe.aspect.GenericResponse;
import com.firisbe.aspect.encryption.Encryption;
import com.firisbe.error.CustomerNotFoundException;
import com.firisbe.error.PaymentFailedException;
import com.firisbe.error.TransferNotFoundException;
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

@Service
@AllArgsConstructor
public class TransferServiceImplementation implements TransferServiceInterface {
    private final TransferRepository repo;
    private final CustomerServiceImplementation customerService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Encryption encryption;

    @Override
    public GenericResponse<String> sendPaymentMessageToKafka(CustomerPaymentRequest request, String token) {
        try {
            Customer senderAccount = customerService.findCustomerToToken(token);
            Customer receiveAccount = customerService.findCustomerByMail(request.receiveMail());

            if (senderAccount != null && receiveAccount != null && senderAccount.getCreditCardNumber() != null && receiveAccount.getCreditCardNumber() != null) {
                String message = senderAccount.getId() + "/" + receiveAccount.getId() + "/" + request.amount();

                kafkaTemplate.send("payment_process", message);
                kafkaTemplate.send("payment_log", "Payment request received successfully!");
                return new GenericResponse<>("Payment request received successfully!", true);

            } else {
                throw new PaymentFailedException();
            }
        } catch (PaymentFailedException e){
            kafkaTemplate.send("error_logs", "PaymentFailedException: " + e.getMessage());
            throw new PaymentFailedException(e);
        }
        catch (Exception e) {
            kafkaTemplate.send("error_logs", "RuntimeException: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @Override
    @KafkaListener(
            topics = {"${kafka.topic.paymentProcess}"},
            groupId = "${kafka.groupId}"
    )
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = {PaymentFailedException.class})
    public void processPaymentMessageFromKafka(String request) {
        try {

            String[] parts = request.split("/");

            long senderCustomerId = Long.parseLong(parts[0]);
            long receiverCustomerId = Long.parseLong(parts[1]);
            BigDecimal amount = new BigDecimal(parts[2]);


            Customer senderCustomer = customerService.findById(senderCustomerId);
            Customer receiveCustomer = customerService.findById(receiverCustomerId);

            if (amount.compareTo(BigDecimal.valueOf(0)) > 0 && senderCustomer.getBalance().compareTo(amount) > 0) {
                senderCustomer.setBalance(
                        senderCustomer.getBalance().subtract(amount)
                );
                receiveCustomer.setBalance(
                        receiveCustomer.getBalance().add(amount)
                );

                customerService.saveCustomer(senderCustomer);
                customerService.saveCustomer(receiveCustomer);


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

                kafkaTemplate.send("payment_log", "Payment processed successfully!");
            } else {
                throw new PaymentFailedException();
            }
        }catch (PaymentFailedException e){
            kafkaTemplate.send("error_logs", "PaymentFailedException: Failed payment Reason: " + e);
            throw new PaymentFailedException(e);
        }
        catch (Exception e) {
            kafkaTemplate.send("error_logs", "RuntimeException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = {PaymentFailedException.class})
    public GenericResponse<PaymentResponse> readPaymentForCustomer(String token, Long id) {
        try {
            Transfer transfer = repo.findById(id).orElseThrow(TransferNotFoundException::new);
            Customer customer = customerService.findCustomerToToken(token);
            if (customer != null && transfer.getSender().getId().equals(customer.getId())) {
                PaymentResponse response = new PaymentResponse(
                        transfer.getSender(),
                        transfer.getReceiver(),
                        transfer.getAmount(),
                        transfer.getTimestamp()
                );
                kafkaTemplate.send("payment_log", "Transfer read successfully");
                return new GenericResponse<>(response, true);
            } else {
                throw new CustomerNotFoundException();
            }
        } catch (CustomerNotFoundException e) {
            kafkaTemplate.send("error_logs", "CustomerNotFoundException: " + e.getMessage());
            throw new CustomerNotFoundException(e);
        } catch (TransferNotFoundException e) {
            kafkaTemplate.send("error_logs", "TransferNotFoundException: " + e.getMessage());
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "RuntimeException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllSentPaymentForCustomer(String token) {
        return new GenericResponse<>(listTransfer(token, "sent"), true);
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllPaymentForCustomer(String token) {
        return new GenericResponse<>(listTransfer(token, "all"), true);
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllReceivedPaymentForCustomer(String token, int monthOffset) {
        LocalDateTime referenceDate = LocalDateTime.now().minusMonths(monthOffset);
        List<PaymentResponse> filteredList = listTransfer(token, "received");
        filteredList = filteredList.stream()
                .filter(paymentResponse -> paymentResponse.timestamp().isAfter(referenceDate))
                .toList();
        return new GenericResponse<>(filteredList, true);
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllSentPaymentForCustomer(String token, int monthOffset) {
        LocalDateTime referenceDate = LocalDateTime.now().minusMonths(monthOffset);
        List<PaymentResponse> filteredList = listTransfer(token, "sent");
        filteredList = filteredList.stream()
                .filter(paymentResponse -> paymentResponse.timestamp().isAfter(referenceDate))
                .toList();
        return new GenericResponse<>(filteredList, true);
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllPaymentForCustomer(String token, int monthOffset) {
        LocalDateTime referenceDate = LocalDateTime.now().minusMonths(monthOffset);
        List<PaymentResponse> filteredList = listTransfer(token, "all");
        filteredList = filteredList.stream()
                .filter(paymentResponse -> paymentResponse.timestamp().isAfter(referenceDate))
                .toList();
        return new GenericResponse<>(filteredList, true);
    }

    @Override
    public GenericResponse<PaymentResponse> readPaymentForAdmin(Long id) {
        try {
            Transfer transfer = repo.findById(id).orElseThrow(TransferNotFoundException::new);
            kafkaTemplate.send("payment_log", "Transfer read successfully");
            return new GenericResponse<>(
                    new PaymentResponse(
                            transfer.getSender(),
                            transfer.getReceiver(),
                            transfer.getAmount(),
                            transfer.getTimestamp()
                    ), true
            );
        } catch (TransferNotFoundException e) {
            kafkaTemplate.send("error_logs", "TransferNotFoundException: " + e.getMessage());
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "RuntimeException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllPaymentForAdmin() {
        try {
            List<Transfer> transferList = repo.findAll();
            if (transferList.isEmpty()) {
                throw new TransferNotFoundException();
            }
            List<PaymentResponse> response = transferList.stream()
                    .map(transfer -> new PaymentResponse(
                            transfer.getSender(),
                            transfer.getReceiver(),
                            transfer.getAmount(),
                            transfer.getTimestamp()
                    )).toList();
            kafkaTemplate.send("payment_log", "Transfers listed successfully");
            return new GenericResponse<>(response, true);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "TransferNotFoundException: " );
            throw new TransferNotFoundException(e);
        }
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllPaymentForAdmin(int monthOffset) {
        try {
            LocalDateTime referenceDate = LocalDateTime.now().minusMonths(monthOffset);
            List<Transfer> transferList = repo.findAll();
            if (transferList.isEmpty()) {
                throw new TransferNotFoundException();
            }
            List<PaymentResponse> response = transferList.stream()
                    .map(transfer -> new PaymentResponse(
                            transfer.getSender(),
                            transfer.getReceiver(),
                            transfer.getAmount(),
                            transfer.getTimestamp()
                    )).toList();
            response = response.stream()
                    .filter(paymentResponse -> paymentResponse.timestamp().isAfter(referenceDate))
                    .toList();
            kafkaTemplate.send("payment_log", "Transfers listed successfully");
            return new GenericResponse<>(response, true);
        } catch (TransferNotFoundException e) {
            kafkaTemplate.send("error_logs", "TransferNotFoundException: " + e.getMessage());
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "RuntimeException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllPaymentForAdminByCustomerId(Long customerId) {
        try {
            List<Transfer> transferList = repo.findAll();
            if (transferList.isEmpty()) {
                throw new TransferNotFoundException();
            }
            List<PaymentResponse> response = transferList.stream()
                    .map(transfer -> new PaymentResponse(
                            transfer.getSender(),
                            transfer.getReceiver(),
                            transfer.getAmount(),
                            transfer.getTimestamp()
                    )).toList();

            response = response.stream()
                    .filter(paymentResponse -> paymentResponse.sentCustomer().getId().equals(customerId) || paymentResponse.receiverCustomer().getId().equals(customerId))
                    .toList();
            kafkaTemplate.send("payment_log", "Transfers listed successfully");
            return new GenericResponse<>(response, true);
        } catch (TransferNotFoundException e) {
            kafkaTemplate.send("error_logs", "TransferNotFoundException: " + e.getMessage());
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "RuntimeException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllPaymentForAdminByCardNumber(String cardNumber) {
        try {
            List<Transfer> transferList = repo.findAll();
            if (transferList.isEmpty()) {
                throw new TransferNotFoundException();
            }
            List<PaymentResponse> response = transferList.stream()
                    .map(transfer -> new PaymentResponse(
                            transfer.getSender(),
                            transfer.getReceiver(),
                            transfer.getAmount(),
                            transfer.getTimestamp()
                    )).toList();

            response = response.stream()
                    .filter(paymentResponse -> encryption.decrypt(paymentResponse.sentCustomer().getCreditCardNumber()).equals(cardNumber) || encryption.decrypt(paymentResponse.receiverCustomer().getCreditCardNumber()).equals(cardNumber))
                    .toList();

            kafkaTemplate.send("payment_log", "Transfers listed successfully");
            return new GenericResponse<>(response, true);
        } catch (TransferNotFoundException e) {
            kafkaTemplate.send("error_logs", "TransferNotFoundException: " + e.getMessage());
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "RuntimeException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllPaymentForAdminByCustomerName(String customerName) {
        try {
            List<Transfer> transferList = repo.findAll();
            if (transferList.isEmpty()) {
                throw new TransferNotFoundException();
            }
            List<PaymentResponse> response = transferList.stream()
                    .map(transfer -> new PaymentResponse(
                            transfer.getSender(),
                            transfer.getReceiver(),
                            transfer.getAmount(),
                            transfer.getTimestamp()
                    )).toList();

            response = response.stream()
                    .filter(paymentResponse -> paymentResponse.sentCustomer().getName().equals(customerName) || paymentResponse.receiverCustomer().getName().equals(customerName))
                    .toList();

            kafkaTemplate.send("payment_log", "Transfers listed successfully");
            return new GenericResponse<>(response, true);
        } catch (TransferNotFoundException e) {
            kafkaTemplate.send("error_logs", "TransferNotFoundException: " + e.getMessage());
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "RuntimeException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public GenericResponse<List<PaymentResponse>> readAllPaymentForAdminWithDateInterval(int startDate, int endDate) {
        try {
            List<Transfer> transferList = repo.findAll();
            LocalDateTime startDateInterval = LocalDateTime.now().minusMonths(startDate);
            LocalDateTime endDateInterval = LocalDateTime.now().minusMonths(endDate);
            if (transferList.isEmpty()) {
                throw new TransferNotFoundException();
            }
            List<PaymentResponse> response = transferList.stream()
                    .map(transfer -> new PaymentResponse(
                            transfer.getSender(),
                            transfer.getReceiver(),
                            transfer.getAmount(),
                            transfer.getTimestamp()
                    )).toList();


            response = response.stream()
                    .filter(paymentResponse -> paymentResponse.timestamp().isAfter(startDateInterval)
                            &&
                            paymentResponse.timestamp().isBefore(endDateInterval))
                    .toList();

            kafkaTemplate.send("payment_log", "Transfers listed successfully");
            return new GenericResponse<>(response, true);
        } catch (TransferNotFoundException e) {
            kafkaTemplate.send("error_logs", "TransferNotFoundException: " + e.getMessage());
            throw new TransferNotFoundException(e);
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "RuntimeException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /*
     *     Bu method Customer 'ın transfer işlemlerini ne şekilde yapacağını belirtmek için oluşturulmuştur. 3 tane fonksiyonda benzer
     *     işlemler yapıldığı için yapıların sadeleştirilmesi için oluşturulmuştur.
     */
    public List<PaymentResponse> listTransfer(String token, String value) {
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
            if (transferList.isEmpty()) {
                kafkaTemplate.send("error_logs", "TransferNotFoundException: No transfer found for customer");
                throw new TransferNotFoundException();
            }

            List<PaymentResponse> response = transferList.stream().map(
                    transfer -> new PaymentResponse(
                            transfer.getSender(),
                            transfer.getReceiver(),
                            transfer.getAmount(),
                            transfer.getTimestamp()
                    )
            ).toList();
            kafkaTemplate.send("payment_log", "Transfers listed successfully");
            return response;
        } catch (TransferNotFoundException e) {
            kafkaTemplate.send("error_logs", "TransferNotFoundException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            kafkaTemplate.send("error_logs", "RuntimeException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    @Override
    public GenericResponse<List<PaymentResponse>> readAllReceivedPaymentForCustomer(String token) {
        return new GenericResponse<>(listTransfer(token, "received"), true);
    }

}
