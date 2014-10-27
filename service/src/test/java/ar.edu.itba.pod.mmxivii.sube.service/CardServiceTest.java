package ar.edu.itba.pod.mmxivii.sube.service;

import ar.edu.itba.pod.mmxivii.sube.common.Card;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;
import ar.edu.itba.pod.mmxivii.sube.server.CardRegistryImpl;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;

import static ar.edu.itba.pod.mmxivii.sube.common.TestUtils.TEST_CARD_HOLDER;
import static ar.edu.itba.pod.mmxivii.sube.common.TestUtils.TEST_LABEL;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Pablo on 26/10/14.
 */
public class CardServiceTest {

    private CardServiceImpl cardService = null;
    private CardRegistry cardRegistry = null;

    @Before
    public void setUp() throws Exception {
        cardRegistry = new CardRegistryImpl();
        cardService = new CardServiceImpl(cardRegistry);
        Utils.skipDelay(true);
    }

    @Test
    public void amountsTest() {
        try {
            final Card card = cardRegistry.newCard(TEST_CARD_HOLDER, TEST_LABEL);
            assertThat(card).isNotNull();
            assertThat(card.getCardHolder()).isEqualTo(TEST_CARD_HOLDER);

            final double balance = cardService.getCardBalance(card.getId());
            assertThat(balance).isEqualTo(0d);

            final double value = 44d;
            final double newValue = cardService.recharge(card.getId(), "test", value);
            assertThat(newValue).isEqualTo(value);

            assertThat(cardService.getCardBalance(card.getId())).isEqualTo(value);

            assertThat(cardService.recharge(card.getId(), "test", 99d)).isEqualTo(CardRegistry.OPERATION_NOT_PERMITTED_BY_BALANCE);
            assertThat(cardService.travel(card.getId(), "test", 99d)).isEqualTo(CardRegistry.OPERATION_NOT_PERMITTED_BY_BALANCE);

            assertThat(cardService.getCardBalance(card.getId())).isEqualTo(value);
        } catch (RemoteException ignored) {
        }
    }

//    @Test
//    public void concurrencyBaseTest() {
//        new MultithreadingTester().add(new RunnableAssert("testing") {
//            @Override
//            public void run() throws Exception {
//                final String cardHolder = randomString(TEST_CARD_HOLDER);
//                final String label = randomString(TEST_LABEL);
//                final Card card = cardRegistry.newCard(cardHolder, label);
//
//                assertThat(card.getCardHolder()).isEqualTo(cardHolder);
//                assertThat(card.getLabel()).isEqualTo(label);
//
//                final Card other = cardRegistry.getCard(card.getId());
//                assertThat(other).isEqualTo(card);
//
//                double balance = cardRegistry.getCardBalance(card.getId());
//                assertThat(balance).isEqualTo(0d);
//
//                for (int i = 0; i < 10; i++) {
//                    final double newBalance = cardRegistry.addCardOperation(card.getId(), "nada", i);
//                    assertThat(newBalance).isEqualTo(balance + i);
//                    assertThat(cardRegistry.getCardBalance(card.getId())).isEqualTo(newBalance);
//                    balance = newBalance;
//                }
//            }
//        }).run();
//    }
}
