package indi.qjx.base.mvvm;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;



import java.lang.reflect.ParameterizedType;

import indi.qjx.base.mvvm.viewmodel.BaseViewModel;


public abstract class BaseMvvmActivity<VM extends BaseViewModel, DB extends ViewDataBinding> extends AppCompatActivity {
    protected VM mViewModel;
    protected DB mViewDataBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        mViewDataBind = DataBindingUtil.setContentView(this, getLayoutId());
        // 让 LiveData 和 xml 可以双向绑定
        mViewDataBind.setLifecycleOwner(this);

        initCommonView();
        initViewModel();
        bindViewModel();

        init();

        // 让 ViewModel 可以感知 Activity 的生命周期
        if (mViewModel != null) {
            getLifecycle().addObserver(mViewModel);
        }
    }

    /**
     * 当前页面的布局资源ID
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
