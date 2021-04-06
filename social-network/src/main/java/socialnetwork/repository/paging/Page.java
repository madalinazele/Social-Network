package socialnetwork.repository.paging;

import java.util.stream.Stream;

public interface Page <E>{
    Pageable getPageable();
    Pageable getNextPageable();

    Stream<E> getContent();
}
