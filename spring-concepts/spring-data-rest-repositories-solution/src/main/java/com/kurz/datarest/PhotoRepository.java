package com.kurz.datarest;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(path = "photos", collectionResourceRel = "photos", itemResourceRel = "photos")
public interface PhotoRepository extends CrudRepository<Photo, Long> {

    List<Photo> findByPhotographerIgnoreCase(@Param("photographer") String photographer);

    @Override
    @RestResource(exported = false)
    void deleteById(Long id);
}
