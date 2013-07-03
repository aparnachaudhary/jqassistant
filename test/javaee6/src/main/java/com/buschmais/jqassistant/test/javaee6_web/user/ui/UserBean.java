package com.buschmais.jqassistant.test.javaee6_web.user.ui;

import com.buschmais.jqassistant.test.javaee6_web.user.logic.api.UserService;
import com.buschmais.jqassistant.test.javaee6_web.user.persistence.api.model.User;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
@Named
@ConversationScoped
public class UserBean implements Serializable {

    @Inject
    private UserService userService;

    @Inject
    private Conversation conversation;

    private User user;

    public List<User> getPersonList() {
        return userService.getUsers();
    }

    public String onCreate() {
        this.user = new User();
        this.conversation.begin();
        return "/edit";
    }

    public String onEdit(User user) {
        this.user = user;
        this.conversation.begin();
        return "/edit";
    }

    public String onSave() {
        this.conversation.end();
        if (this.user.getId() == null) {
            this.userService.create(this.user);
        } else {
            this.userService.update(this.user);
        }
        return "/list";
    }

    public String onDelete(User user) {
        this.userService.delete(user);
        return "/list";
    }

}
