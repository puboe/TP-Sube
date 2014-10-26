package ar.edu.itba.pod.mmxivii.sube.service;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.rmi.server.UID;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Pablo on 18/10/14.
 */
class Operation implements Serializable {

    private Date timestamp;
    private double amount;
    private UID cardId;

    Operation(@Nonnull UID cardId, double amount) {
        this(cardId, amount, Calendar.getInstance().getTime());
    }

    Operation(@Nonnull UID cardId, double amount, Date timestamp) {
        this.amount = amount;
        this.cardId =  cardId;
        this.timestamp = timestamp;
    }

    Date getTimestamp() {
        return timestamp;
    }

    double getAmount() {
        return amount;
    }

    UID getCardId() {
        return cardId;
    }

    @Override
    public String toString() {
        return "Id tarjeta: " + cardId + ", Monto: " + amount + ", Timestamp: " + timestamp;
    }
}
