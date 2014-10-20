package ar.edu.itba.pod.mmxivii.sube.service;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Pablo on 19/10/14.
 */
class CardState implements Serializable {

    double amount;
    Date timestamp;

    CardState(double amount, Date timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }

    void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    Date getTimestamp() {
        return timestamp;
    }

    void setAmount(double amount) {
        this.amount = amount;
    }

    double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Balance: " + amount + ", Timestamp: " + timestamp;
    }
}
