/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.richfaces.photoalbum.test.manager;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.richfaces.photoalbum.bean.UserBean;
import org.richfaces.photoalbum.domain.User;
import org.richfaces.photoalbum.service.Constants;
import org.richfaces.photoalbum.util.Utils;

@RunWith(Arquillian.class)
public class UserBeanTest {

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war").addPackage(User.class.getPackage()).addClass(UserBean.class)
            .addClass(Utils.class).addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml") // important
            .addAsWebInfResource("test-ds.xml").addAsResource("importusers.sql", "import.sql");
    }

    @Inject
    UserBean bean;

    @Inject
    EntityManager em;

    @Inject
    UserTransaction utx;

    @Before
    public void startTransaction() throws Exception {
        utx.begin();
        em.joinTransaction();
    }

    @After
    public void commitTransaction() throws Exception {
        utx.commit();
    }

    @Test
    public void isUserLoggedIn() throws Exception {
        User user = (User) em.createNamedQuery(Constants.USER_LOGIN_QUERY).setParameter(Constants.USERNAME_PARAMETER, "Noname")
            .setParameter(Constants.PASSWORD_PARAMETER, "8cb2237d0679ca88db6464eac60da96345513964").getSingleResult();

        Assert.assertNotNull(user);

        User loggedInUser = bean.logIn("Noname", "8cb2237d0679ca88db6464eac60da96345513964");

        Assert.assertNotNull(loggedInUser);
        Assert.assertNotNull(bean.getUser());
        Assert.assertEquals(loggedInUser, bean.getUser());
    }

    @Test
    public void isUserRefreshed() throws Exception {
        em.createQuery("update User u set u.email = :email where u.login = :login").setParameter("email", "mail@mail.net")
            .setParameter("login", "Noname").executeUpdate();

        bean.refreshUser();

        Assert.assertTrue("mail: " + bean.getUser().getEmail() + " = 'mail@mail.net'",
            "mail@mail.net".equals(bean.getUser().getEmail()));
    }
}
