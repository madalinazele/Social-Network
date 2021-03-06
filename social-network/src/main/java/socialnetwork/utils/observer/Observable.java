package socialnetwork.utils.observer;

public interface Observable<E> {
    void addObserver(Observer<E> e);
    void removeObserver(Observer<E> e);
    void notifyObservers(E t);
}