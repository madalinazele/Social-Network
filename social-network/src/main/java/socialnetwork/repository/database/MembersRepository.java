package socialnetwork.repository.database;

import socialnetwork.domain.Entity;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.paging.PagingRepository;

public interface MembersRepository<ID, E extends Entity<ID>, T> extends PagingRepository<ID, E> {

    /**
     * @return all entities
     */
    Iterable<T> findAll(E entity);

    /**
     * @param entity entity must be not null
     * @return null- if the given entity is saved
     * otherwise returns the entity (id already exists)
     * @throws ValidationException if the entity is null or not valid
     */
    E addMember(E entity, T member);

    /**
     * @param entity entity must be not null
     * @return null- if the given entity is saved
     * otherwise returns the entity (id already exists)
     * @throws ValidationException if the entity is null or not valid
     */
    E updateMember(E entity, T member);


    /**
     * removes the entity with the specified id
     *
     * @return the removed entity or null if there is no entity with the given id
     * @throws ValidationException if the given id is null.
     */
    E deleteMember(E entity, T member);
}
