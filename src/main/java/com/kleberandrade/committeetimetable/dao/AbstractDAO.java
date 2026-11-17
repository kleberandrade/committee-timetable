/* Copyright 2018 Kleber de Oliveira Andrade. MIT license; see LICENSE. */
package com.kleberandrade.committeetimetable.dao;

import com.kleberandrade.committeetimetable.utils.EntityManagerUtil;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

/**
 * Each call owns one manager/transaction. Failures propagate after rollback. Returned entities are
 * detached.
 */
public abstract class AbstractDAO<T extends Serializable> implements IGenericDAO<T> {
  private final Class<T> type;
  private final EntityManagerFactory factory;

  protected AbstractDAO(Class<T> type) {
    this(type, null);
  }

  protected AbstractDAO(Class<T> type, EntityManagerFactory factory) {
    this.type = type;
    this.factory = factory;
  }

  private <R> R transaction(Function<EntityManager, R> action) {
    try (var manager =
        factory == null ? EntityManagerUtil.getEntityManager() : factory.createEntityManager()) {
      var tx = manager.getTransaction();
      try {
        tx.begin();
        R result = action.apply(manager);
        tx.commit();
        return result;
      } catch (RuntimeException failure) {
        if (tx.isActive()) tx.rollback();
        throw failure;
      }
    }
  }

  @Override
  public T findById(Long id) {
    Objects.requireNonNull(id);
    return transaction(em -> em.find(type, id));
  }

  @Override
  public List<T> findAll() {
    return transaction(
        em -> {
          var criteria = em.getCriteriaBuilder().createQuery(type);
          criteria.select(criteria.from(type));
          return em.createQuery(criteria).getResultList();
        });
  }

  @Override
  public void create(T entity) {
    Objects.requireNonNull(entity);
    transaction(
        em -> {
          em.persist(entity);
          return null;
        });
  }

  @Override
  public void update(T entity) {
    Objects.requireNonNull(entity);
    transaction(
        em -> {
          em.merge(entity);
          return null;
        });
  }

  @Override
  public void delete(T entity) {
    Objects.requireNonNull(entity);
    transaction(
        em -> {
          em.remove(em.merge(entity));
          return null;
        });
  }

  @Override
  public void deleteById(Long id) {
    Objects.requireNonNull(id);
    transaction(
        em -> {
          em.remove(em.getReference(type, id));
          return null;
        });
  }
}
