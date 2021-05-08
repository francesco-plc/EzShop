package it.polito.ezshop.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import it.polito.ezshop.data.TicketEntry;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@DatabaseTable(tableName = "sale_transactions")
public class SaleTransaction implements it.polito.ezshop.data.SaleTransaction {
    public enum StatusEnum {STARTED, CLOSED, PAID}

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private StatusEnum status = StatusEnum.STARTED;

    @DatabaseField(canBeNull = false)
    private Date date = new Date(LocalDate.now().toEpochDay());

    @DatabaseField(canBeNull = false)
    private String time = LocalTime.now().toString();

    @DatabaseField(canBeNull = false)
    private double amount = 0;

    @DatabaseField(canBeNull = false, columnName = "discount_rate")
    private double discountRateAmount = 0;

    @DatabaseField(columnName = "payment_type")
    private String paymentType;

    @DatabaseField()
    private double cash;

    @DatabaseField()
    private double change;

    @DatabaseField(columnName = "credit_card")
    private String creditCard;

    @ForeignCollectionField(eager = true)
    ForeignCollection<SaleTransactionRecord> records;

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
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return LocalTime.parse(time);
    }

    public void setTime(LocalTime time) {
        this.time = time.toString();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getDiscountRateAmount() {
        return discountRateAmount;
    }

    public void setDiscountRateAmount(double discountRateAmount) {
        this.discountRateAmount = discountRateAmount;
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

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
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
        return null;
    }

    @Override
    public void setEntries(List<TicketEntry> entries) {

    }

    @Override
    public double getDiscountRate() {
        return getDiscountRateAmount();
    }

    @Override
    public void setDiscountRate(double discountRate) {
        setDiscountRateAmount(discountRate);
    }

    @Override
    public double getPrice() {
        return getAmount();
    }

    @Override
    public void setPrice(double price) {
        setAmount(price);
    }

    public boolean addSaleTransactionRecord(ProductType product, int quantity) throws SQLException {
        // First check for an existing record for this product
        SaleTransactionRecord transactionRecord = this.records.getDao().queryBuilder()
                .where().eq("product_type_id", product.getId()).queryForFirst();

        if (transactionRecord == null) {
            // No existing record for this product, creating a new one
            transactionRecord = new SaleTransactionRecord(product, quantity);
            this.records.add(transactionRecord);
        } else {
            // There is an existing record for this product, increasing quantity
            transactionRecord.setQuantity(transactionRecord.getQuantity() + quantity);
            this.records.update(transactionRecord);
        }

        return true;
    }
}
