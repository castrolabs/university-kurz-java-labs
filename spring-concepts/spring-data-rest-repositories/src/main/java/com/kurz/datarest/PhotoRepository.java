package com.kurz.datarest;

import org.springframework.data.repository.CrudRepository;

// TODO-01: Spring Data REST derives an endpoint's path and relation name by
// pluralizing the entity's simple class name -- and its pluralizer isn't
// infallible. "Photo" becomes "/photoes", not "/photos". Fix it with
// @RepositoryRestResource, applied on this interface:
//
//   @RepositoryRestResource(path = "photos", collectionResourceRel = "photos",
//           itemResourceRel = "photos")
//
// TODO-02: Add a derived query method exposing photos by photographer as a
// search resource:
//
//   List<Photo> findByPhotographerIgnoreCase(@Param("photographer") String photographer);
//
// Spring Data REST exposes it automatically at
// /photos/search/findByPhotographerIgnoreCase?photographer=... -- no
// controller, no @Query, just the method signature. The @Param annotation
// is required here: without -parameters compiler output, Spring Data can't
// otherwise recover the "photographer" name to bind the query parameter to.
public interface PhotoRepository extends CrudRepository<Photo, Long> {
}
