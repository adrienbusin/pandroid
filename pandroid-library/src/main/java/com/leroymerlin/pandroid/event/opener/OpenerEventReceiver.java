package com.leroymerlin.pandroid.event.opener;

import android.app.Activity;
import android.app.Application;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.Log;

import com.leroymerlin.pandroid.PandroidApplication;
import com.leroymerlin.pandroid.R;
import com.leroymerlin.pandroid.app.PandroidFragment;
import com.leroymerlin.pandroid.app.delegate.PandroidDelegateProvider;
import com.leroymerlin.pandroid.dagger.PandroidDaggerProvider;
import com.leroymerlin.pandroid.event.EventBusManager;
import com.leroymerlin.pandroid.log.LogWrapper;
import com.leroymerlin.pandroid.log.LogcatLogger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Abstract EventBusReceiver in charge of receiving Opener event
 * <p>
 * Created by florian on 12/11/14.
 */
public abstract class OpenerEventReceiver<V extends OpenerReceiverProvider, T extends Opener> implements EventBusManager.EventBusReceiver {

    private final static String TAG = "OpenerEventReceiver";

    protected List<String> filter = new ArrayList<String>();

    protected WeakReference<V> refAttachedObject;

    @Inject
    protected EventBusManager eventBusManager;
    @Inject
    protected LogWrapper logWrapper;

    @Override
    public List<String> getTags() {
        return null;
    }

    @Override
    public boolean handle(Object data) {
        if (data instanceof Opener && filter.contains(((Opener) data).getFilterTag())) {
            onOpenerReceived((T) data);
            return true;
        }
        return false;
    }

    protected abstract void onOpenerReceived(T opener);


    protected FragmentManager getFragmentManager() {
        if (refAttachedObject != null && refAttachedObject.get() != null) {
            return refAttachedObject.get().provideFragmentManager();
        }
        return null;
    }

    protected Activity getActivity() {
        if (refAttachedObject != null && refAttachedObject.get() != null) {
            return refAttachedObject.get().provideActivity();
        }
        return null;
    }

    private void add(String tag) {
        this.filter.add(tag);
    }

    public <T extends OpenerEventReceiver> T add(Class targetClass) {
        add(Opener.createFilter(targetClass));
        return (T) this;
    }

    public <T extends OpenerEventReceiver> T add(Opener opener) {
        add(opener.getFilterTag());
        return (T) this;
    }

    public void attach(V attachedObject) {
        Application application = attachedObject.provideActivity().getApplication();
        if (application instanceof PandroidDaggerProvider) {
            ((PandroidDaggerProvider) application).inject(this);
        } else {
            throw new IllegalStateException("Your application needs to implement PandroidDaggerProvider to use " + TAG);
        }
        this.refAttachedObject = new WeakReference<V>(attachedObject);
    }

    public void detach() {
        refAttachedObject = null;
    }

}
