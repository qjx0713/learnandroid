package indi.qjx.base.mvvm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.ParameterizedType;


public abstract class BaseMvvmFragment<VM extends ViewModel & LifecycleObserver, DB extends ViewDataBinding> extends Fragment {
    protected VM mViewModel;
    protected DB mViewDataBind;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            handleArguments(args);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewDataBind = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);

        initCommonView();
        initViewModel();
        bindViewModel();

        init();

        // 让 ViewModel 可以感知 Fragment 的生命周期
        if (mViewModel != null) {
            getLifecycle().addObserver(mViewModel);
        }

        return mViewDataBind.getRoot();
    }

    /**
     * 处理参数
     *
     * @param args 参数容器
     */
    protected void handleArguments(Bundle args) {

    }

    /**
     * 获取当前页面的布局资源ID
     *
     * @return 布局资源ID
     */
    protected abstract int getLayoutId();

    /**
     * 用于初始化通用view，比如AppBar
     */
    protected void initCommonView() {

    }

    /**
     * 初始化ViewModel
     */
    protected void initViewModel() {
        //获得泛型参数的实际类型
        Class<VM> vmClass = (Class<VM>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        mViewModel = new ViewModelProvider(this).get(vmClass);
    }

    /**
     * 绑定ViewModel
     */
    protected abstract void bindViewModel();

    /**
     * 初始化
     */
    protected abstract void init();
}
