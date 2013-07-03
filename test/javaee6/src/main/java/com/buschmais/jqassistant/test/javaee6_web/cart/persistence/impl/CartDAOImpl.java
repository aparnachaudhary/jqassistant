package com.buschmais.jqassistant.test.javaee6_web.cart.persistence.impl;

import com.buschmais.jqassistant.test.javaee6_web.cart.persistence.api.CartDAO;
import com.buschmais.jqassistant.test.javaee6_web.cart.persistence.api.model.Cart;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
public class CartDAOImpl implements CartDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(Cart cart) {
        entityManager.persist(cart);
    }

    @Override
    public Cart update(Cart cart) {
        return entityManager.merge(cart);
    }

    @Override
    public void delete(Cart cart) {
        entityManager.remove(cart);
    }

    @Override
    public List<Cart> findAll() {
        return entityManager.createQuery("SELECT c FROM Cart c", Cart.class).getResultList();
    }
}
