package com.example.foodorder.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodorder.ControllerApplication;
import com.example.foodorder.R;
import com.example.foodorder.activity.MainActivity;
import com.example.foodorder.adapter.CartAdapter;
import com.example.foodorder.common.Common;
import com.example.foodorder.constant.Constant;
import com.example.foodorder.constant.GlobalFuntion;
import com.example.foodorder.database.FoodDatabase;
import com.example.foodorder.databinding.FragmentCartBinding;
import com.example.foodorder.event.ReloadListCartEvent;
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Order;
import com.example.foodorder.utils.StringUtil;
import com.example.foodorder.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CartFragment extends BaseFragment {

    private FragmentCartBinding mFragmentCartBinding;
    private CartAdapter mCartAdapter;
    private List<Food> mListFoodCart;
    private int mAmount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentCartBinding = FragmentCartBinding.inflate(inflater, container, false);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        displayListFoodInCart();
        mFragmentCartBinding.tvOrderCart.setOnClickListener(v -> onClickOrderCart());

        return mFragmentCartBinding.getRoot();
    }

    @Override
    protected void initToolbar() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setToolBar(false, getString(R.string.cart));
        }
    }

    private void displayListFoodInCart() {
        if (getActivity() == null) {
            return;
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mFragmentCartBinding.rcvFoodCart.setLayoutManager(linearLayoutManager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mFragmentCartBinding.rcvFoodCart.addItemDecoration(itemDecoration);

        initDataFoodCart();
    }

    private void initDataFoodCart() {
        mListFoodCart = new ArrayList<>();
        mListFoodCart = FoodDatabase.getInstance(getActivity()).foodDAO().getListFoodCart();
        if (mListFoodCart == null || mListFoodCart.isEmpty()) {
            return;
        }

        mCartAdapter = new CartAdapter(mListFoodCart, new CartAdapter.IClickListener() {
            @Override
            public void clickDeteteFood(Food food, int position) {
                deleteFoodFromCart(food, position);
            }

            @Override
            public void updateItemFood(Food food, int position) {
                FoodDatabase.getInstance(getActivity()).foodDAO().updateFood(food);
                mCartAdapter.notifyItemChanged(position);

                calculateTotalPrice();
            }
        });
        mFragmentCartBinding.rcvFoodCart.setAdapter(mCartAdapter);

        calculateTotalPrice();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void clearCart() {
        if (mListFoodCart != null) {
            mListFoodCart.clear();
        }
        mCartAdapter.notifyDataSetChanged();
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        List<Food> listFoodCart = FoodDatabase.getInstance(getActivity()).foodDAO().getListFoodCart();
        if (listFoodCart == null || listFoodCart.isEmpty()) {
            String strZero = 0 + Constant.CURRENCY;
            mFragmentCartBinding.tvTotalPrice.setText(strZero);
            mAmount = 0;
            return;
        }

        int totalPrice = 0;
        for (Food food : listFoodCart) {
            totalPrice = totalPrice + food.getTotalPrice();
        }

        String strTotalPrice = totalPrice + Constant.CURRENCY;
        mFragmentCartBinding.tvTotalPrice.setText(strTotalPrice);
        mAmount = totalPrice;
    }

    private void deleteFoodFromCart(Food food, int position) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.confirm_delete_food))
                .setMessage(getString(R.string.message_delete_food))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    FoodDatabase.getInstance(getActivity()).foodDAO().deleteFood(food);
                    mListFoodCart.remove(position);
                    mCartAdapter.notifyItemRemoved(position);

                    calculateTotalPrice();
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void onClickOrderCart() {
        if (getActivity() == null) {
            return;
        }

        if (mListFoodCart == null || mListFoodCart.isEmpty()) {
            GlobalFuntion.showToastMessage(getActivity(),"Bạn chưa mua");
            return;
        }

        @SuppressLint("InflateParams") View viewDialog = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_order, null);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        // init ui
        TextView tvFoodsOrder = viewDialog.findViewById(R.id.tv_foods_order);
        TextView tvPriceOrder = viewDialog.findViewById(R.id.tv_price_order);
        TextView edtNameOrder = viewDialog.findViewById(R.id.edt_name_order);
        TextView edtPhoneOrder = viewDialog.findViewById(R.id.edt_phone_order);
        TextView edtAddressOrder = viewDialog.findViewById(R.id.edt_address_order);
        TextView tvCancelOrder = viewDialog.findViewById(R.id.tv_cancel_order);
        TextView tvCreateOrder = viewDialog.findViewById(R.id.tv_create_order);
        edtNameOrder.setText(Common.currentUser.getName());
        edtPhoneOrder.setText(Common.currentUser.getPhone());
        // Set data
        tvFoodsOrder.setText(getStringListFoodsOrder());
        tvPriceOrder.setText(mFragmentCartBinding.tvTotalPrice.getText().toString());

        // Set listener
        tvCancelOrder.setOnClickListener(v -> bottomSheetDialog.dismiss());

        tvCreateOrder.setOnClickListener(v -> {
            String strName = edtNameOrder.getText().toString().trim();
            String strPhone = edtPhoneOrder.getText().toString().trim();
            String strAddress = edtAddressOrder.getText().toString().trim();

            if (StringUtil.isEmpty(strName) || StringUtil.isEmpty(strPhone) || StringUtil.isEmpty(strAddress)) {
                GlobalFuntion.showToastMessage(getActivity(), getString(R.string.message_enter_infor_order));
            } else {
                long id = System.currentTimeMillis();
                Order order = new Order(id, strName, strPhone, strAddress,
                        mAmount, getStringListFoodsOrder(), Constant.TYPE_PAYMENT_CASH);
                ControllerApplication.get(getActivity()).getBookingDatabaseReference()
//                        .child(Utils.getDeviceId(getActivity()))
                        .child(strPhone)
                        .child(String.valueOf(id))
                        .setValue(order, (error1, ref1) -> {
                            GlobalFuntion.showToastMessage(getActivity(), getString(R.string.msg_order_success));
                            GlobalFuntion.hideSoftKeyboard(getActivity());
                            bottomSheetDialog.dismiss();

                            FoodDatabase.getInstance(getActivity()).foodDAO().deleteAllFood();
                            clearCart();
                        });

                sendNotification(getActivity());

            }
        });

        bottomSheetDialog.show();
    }

    private void sendNotification(Context ctx) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CHANNEL_ID = "my_channel_01";
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(bitmap)
                .setColor(getResources().getColor(R.color.blue))
                .setContentTitle("Chúc mừng")
                .setContentText("Bạn đã đặt hàng thành công");

        Intent resultIntent = new Intent(ctx, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(getNotificationId(), builder.build());
    }

    private int getNotificationId() {
        return (int) new Date().getTime();
    }
    private String getStringListFoodsOrder() {
        if (mListFoodCart == null || mListFoodCart.isEmpty()) {
            return "";
        }
        String result = "";
        for (Food food : mListFoodCart) {
            if (StringUtil.isEmpty(result)) {
                result = "- " + food.getName() + " (" + food.getRealPrice() + Constant.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + food.getCount();
            } else {
                result = result + "\n" + "- " + food.getName() + " (" + food.getRealPrice() + Constant.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + food.getCount();
            }
        }
        return result;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReloadListCartEvent event) {
        displayListFoodInCart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
