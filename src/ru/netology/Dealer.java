package ru.netology;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Dealer {
    public List<Car> availableCars = new ArrayList<>();
    public List<Car> producedCars = new ArrayList<>();

    Lock warehouse = new ReentrantLock(true);
    Condition condition = warehouse.newCondition();

    private static final int PRODUCTION_TIME = 2000;
    private static final int WAREHOUSE_LIMIT = 4;
    private static final int WAITING_TIME = 100;
    private static final int PURCHASE_TIME = 4000;
    private static final int AMOUNT_OF_DEALS = 10;
    private int carsSold;


    public void buyCar() {
        try {
            warehouse.lock();
            String customerName = Thread.currentThread().getName();
            System.out.printf("%s пришел покупать авто.\n", customerName);

            while (availableCars.size() == 0) {
                System.out.printf("%s расстроен, cейчас машин в наличии нет.\n", customerName);
                condition.await();
            }

            Thread.sleep(PURCHASE_TIME);
            carsSold++;
            availableCars.remove(0);
            System.out.printf("%s уехал домой на новеньком авто.\n", customerName);
            System.out.printf("Вcего продано машин: %d.\n", carsSold);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            warehouse.unlock();
        }
    }

    public void deliveryFromWarehouse() {
        while (carsSold < AMOUNT_OF_DEALS) {
            while (availableCars.size() < WAREHOUSE_LIMIT) {
                warehouse.lock();
                try {
                    if (producedCars.size() > 0) {

                        availableCars.add(producedCars.remove(0));
                        System.out.printf("Авто доставлено на склад. На складе %d авто\n", availableCars.size());
                        condition.signal();
                    } else {
                        Thread.sleep(WAITING_TIME);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    warehouse.unlock();
                }
            }
        }
    }

    public void production() {
        String manufacturerName = Thread.currentThread().getName();

        while (carsSold < AMOUNT_OF_DEALS) {
            producedCars.add(new Car());
            System.out.printf("Производитель %s выпустил авто. В наличии всего %d\n",
                    manufacturerName, producedCars.size());
            try {
                Thread.sleep(PRODUCTION_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
