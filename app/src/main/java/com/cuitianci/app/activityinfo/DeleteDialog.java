package com.cuitianci.app.activityinfo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by 77214 on 2018/6/18.
 */

public class DeleteDialog extends DialogFragment implements View.OnClickListener  {


    public DeleteDialog.DeleteListener deleteListener;
    public DeleteDialog.CancelListener cancelListener;
    private TextView tv_delete;
    private TextView tv_cancel;
    private Dialog dialog;

    public DeleteDialog(){

    }
    @SuppressLint("ValidFragment")
    public DeleteDialog( DeleteDialog.DeleteListener deleteListener, DeleteDialog.CancelListener cancelListener) {//提示文字
        this.deleteListener = deleteListener;
        this.cancelListener = cancelListener;
    }


    public interface DeleteListener {
        void delete();
    }

    public interface CancelListener {
        void cancel();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙。
        dialog = new Dialog(getActivity(), R.style.Comment_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定
        View contentView = View.inflate(getActivity(), R.layout.dialog_delete, null);
        dialog.setContentView(contentView);
        dialog.setCanceledOnTouchOutside(true); // 外部点击取消

        // 设置宽度为屏宽, 靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM; // 紧贴底部
        lp.alpha = 1;
        lp.dimAmount = 0.0f;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        tv_delete = (TextView)contentView.findViewById(R.id.delete_tv);
        tv_cancel = (TextView)contentView.findViewById(R.id.cancel_tv);

        tv_delete.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);

        return dialog;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.delete_tv:
                deleteListener.delete();
                dismiss();
                break;
            case R.id.cancel_tv:
                cancelListener.cancel();
                dismiss();
                break;
        }
    }
}
