package indi.qjx.libtemplate.mvvm.viewmodel;

import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import indi.qjx.libtemplate.mvvm.config.LoadState;


/**
 * ViewModel 基类
 */
public class BaseViewModel extends ViewModel implements LifecycleObserver, Observable {

    /**
     * 加载状态
     */
    public MutableLiveData<LoadState> loadState = new MutableLiveData<>();
    /**
     * toast内容
     * 样式为NONE 无
     */
    public MutableLiveData<String> toast = new MutableLiveData<>();
    /**
     * toast内容
     * 样式为SUCCESS 成功
     */
    public MutableLiveData<String> toastSuccess = new MutableLiveData<>();
    /**
     * toast内容
     * 样式为FAIL 失败
     */
    public MutableLiveData<String> toastFail = new MutableLiveData<>();
    /**
     * toast内容
     * 样式为WARN 警告
     */
    public MutableLiveData<String> toastWarn = new MutableLiveData<>();

    /**
     * 用于管理可观察回调的实用程序类
     */
    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    /**
     * 通知侦听器此实例的所有属性都已更改。
     */
    public void notifyChange() {
        callbacks.notifyCallbacks(this, 0, null);
    }

    /**
     * 通知侦听器特定属性已更改。 属性的吸气剂
     * 应使用 [Bindable] 标记更改以在
     * `BR` 用作`fieldId`。
     *
     * @param fieldId 为 Bindable 字段生成的 BR id。
     */
    public void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

    public void showLoading() {
        loadState.postValue(LoadState.LOADING);
    }

    public void hideLoading() {
        loadState.postValue(LoadState.SUCCESS);
    }

    public void errorToastShow(String msg) {
        toastFail.postValue(msg);
    }
}
