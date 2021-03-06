package it.polito.ezshop.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import it.polito.ezshop.data.TicketEntry;

import java.sql.SQLException;
import java.time.*;
import java.util.*;

@DatabaseTable(tableName = "sale_transactions")
public class SaleTransaction implements it.polito.ezshop.data.SaleTransaction {

    public enum StatusEnum {STARTED, CLOSED, PAID}

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private StatusEnum status = StatusEnum.STARTED;

    @DatabaseField(canBeNull = false)
    private double amount = 0;

    @DatabaseField(canBeNull = false, columnName = "discount_rate")
    private double discountRate = 0;

    @DatabaseField(columnName = "payment_type")
    private String paymentType;

    @DatabaseField()
    private double cash;

    @DatabaseField()
    private double change;

    @DatabaseField(columnName = "credit_card", foreign = true, foreignAutoRefresh = true)
    private CreditCard creditCard;

    @DatabaseField(canBeNull = false, columnName = "created_at")
    private final long createdAt = new Date().getTime();

    @ForeignCollectionField(eager = true)
    private ForeignCollection<SaleTransactionRecord> records;

    public SaleTransaction() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public Date getDate() {
        return new Date(createdAt);
    }

    public LocalTime getTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), TimeZone.getDefault().toZoneId()).toLocalTime();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public double getDiscountRate() {
        return discountRate;
    }

    @Override
    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    @Override
    public Integer getTicketNumber() {
        return getId();
    }

    @Override
    public void setTicketNumber(Integer ticketNumber) {
        setId(ticketNumber);
    }

    @Override
    public List<TicketEntry> getEntries() {
        return new ArrayList<>(this.records);
    }

    @Override
    public void setEntries(List<TicketEntry> entries) {
        throw new java.lang.UnsupportedOperationException("This method cannot be implemented given the ForeignCollection logic embedded into OrmLite");
    }

    @Override
    public double getPrice() {
        return getAmount();
    }

    @Override
    public void setPrice(double price) {
        setAmount(price);
    }

    public void refreshAmount() {
        double updatedAmount = this.records.stream().mapToDouble(SaleTransactionRecord::getTotalPrice).sum();

        this.amount = updatedAmount * (1 - discountRate);
    }

    public void setRecords(ForeignCollection<SaleTransactionRecord> records) {
        this.records = records;
    }


    public ForeignCollection<SaleTransactionRecord> getRecords() {
        return this.records;
    }

    public boolean addProductToRecords(ProductType product, int addedQuantity) throws SQLException {

        Optional<SaleTransactionRecord> optionalRecord = this.records.stream().filter(
                record -> record.getBarCode().equals(product.getBarCode())
        ).findFirst();

        if (optionalRecord.isPresent()) {
            SaleTransactionRecord existingRecord = optionalRecord.get();

            // There is an existing record for input product, updating it
            int amountAlreadyInTransaction = existingRecord.getAmount();

            if (product.getQuantity() < amountAlreadyInTransaction + addedQuantity) {
                // Not enough products to fulfill
                return false;
            }

            // There is an existing record for input product, increasing quantity
            existingRecord.setAmount(amountAlreadyInTransaction + addedQuantity);
            existingRecord.refreshTotalPrice();

            // Update record
            this.records.update(existingRecord);

        } else {
            // No existing record for input product, creating a new one

            if (product.getQuantity() < addedQuantity) {
                // Not enough products to fulfill
                return false;
            }

            SaleTransactionRecord newRecord = new SaleTransactionRecord(this, product, addedQuantity);

            // Add new record
            this.records.add(newRecord);
        }

        refreshAmount();

        return true;
    }

    public boolean removeProductFromRecords(ProductType product, int removedQuantity) throws SQLException {


        Optional<SaleTransactionRecord> optionalRecord = this.records.stream().filter(
                record -> record.getBarCode().equals(product.getBarCode())
        ).findFirst();

        if (!optionalRecord.isPresent()) {
            // No transaction record for this product
            return false;
        }

        SaleTransactionRecord existingRecord = optionalRecord.get();

        int amountAlreadyInTransaction = existingRecord.getAmount();

        if (removedQuantity > amountAlreadyInTransaction) {
            // The quantity cannot satisfy the request
            return false;
        } else if (removedQuantity == amountAlreadyInTransaction) {

            // Remove record
            this.records.remove(existingRecord);
        } else {
            existingRecord.setAmount(amountAlreadyInTransaction - removedQuantity);
            existingRecord.refreshTotalPrice();

            // Update record
            this.records.update(existingRecord);
        }

        refreshAmount();

        return true;
    }
}
