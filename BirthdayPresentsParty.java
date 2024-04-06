
import java.util.concurrent.locks.ReentrantLock;

class Present {
    int tag;
    Present next;
    ReentrantLock lock = new ReentrantLock();

    Present(int tag) {
        this.tag = tag;
        this.next = null;
    }
}

class ConcurrentPresentList {
    private Present head;

    public ConcurrentPresentList() {
        head = new Present(Integer.MIN_VALUE);
        head.next = new Present(Integer.MAX_VALUE);
    }

    public boolean add(int tag) {
        head.lock.lock();
        Present pred = head;
        try {
            Present curr = pred.next;
            curr.lock.lock();
            try {
                while (curr.tag < tag) {
                    pred.lock.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }
                if (curr.tag == tag) {
                    return false;
                }
                Present newNode = new Present(tag);
                newNode.next = curr;
                pred.next = newNode;
                return true;
            } finally {
                curr.lock.unlock();
            }
        } finally {
            pred.lock.unlock();
        }
    }

    public boolean remove(int tag) {
        Present pred = null, curr = null;
        head.lock.lock();
        try {
            pred = head;
            curr = pred.next;
            curr.lock.lock();
            try {
                while (curr.tag < tag) {
                    pred.lock.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }
                if (curr.tag == tag) {
                    pred.next = curr.next;
                    return true;
                }
                return false;
            } finally {
                curr.lock.unlock();
            }
        } finally {
            pred.lock.unlock();
        }
    }

    public boolean contains(int tag) {
        Present curr = head;
        while (curr.tag < tag) {
            curr = curr.next;
        }
        return curr.tag == tag;
    }
}

public class BirthdayPresentsParty {

    public static void main(String[] args) throws InterruptedException {
        final ConcurrentPresentList presentsList = new ConcurrentPresentList();

        // Number of servants
        int numServants = 4;
        Thread[] servants = new Thread[numServants];
        for (int i = 0; i < numServants; i++) {
            servants[i] = new Thread(() -> {
                for (int j = 0; j < 500000; j++) {
                    int action = j % 3;
                    int presentTag = (int) (Math.random() * 500000);
                    switch (action) {
                        case 0: // Add a present
                            presentsList.add(presentTag);
                            break;
                        case 1: // Remove a present (write a Thank You card)
                            presentsList.remove(presentTag);
                            break;
                        case 2: // Check if a present exists
                            presentsList.contains(presentTag);
                            break;
                    }
                }
            });
            servants[i].start();
        }

        for (int i = 0; i < numServants; i++) {
            servants[i].join();
        }

        System.out.println("All presents processed. Thank you cards written.");
    }
}
