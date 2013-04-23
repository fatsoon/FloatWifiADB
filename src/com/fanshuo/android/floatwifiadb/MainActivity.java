package com.fanshuo.android.floatwifiadb;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	boolean hasFloatView = false;
	boolean isOn = false;
	boolean canMove = false;
	private WindowManager wm = null;
	private WindowManager.LayoutParams wmParams = null;

	private View myFV = null;
	private Button floatButton;

	private float mTouchStartX;
	private float mTouchStartY;
	private float x;
	private float y;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	private void createView() {
		myFV = LayoutInflater.from(this).inflate(R.layout.view_float, null);
		floatButton = (Button) myFV.findViewById(R.id.float_button);
		// 获取WindowManager
		wm = (WindowManager) getApplicationContext().getSystemService("window");
		// 设置LayoutParams(全局变量）相关参数
		wmParams = ((MyApplication) getApplication()).getMywmParams();

		/**
		 * 以下都是WindowManager.LayoutParams的相关属性 具体用途可参考SDK文档
		 */
		wmParams.type = LayoutParams.TYPE_SYSTEM_ERROR; // 设置window type
		wmParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明

		// 设置Window flag
		wmParams.flags = 327976;

		/*
		 * 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
		 * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
		 * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		 */

		wmParams.gravity = Gravity.LEFT | Gravity.TOP; // 调整悬浮窗口至左上角
		// 以屏幕左上角为原点，设置x、y初始值
		wmParams.x = 0;
		wmParams.y = 45;

		wmParams.width = ScreenSizeUtil.Dp2Px(this, 90);
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		floatButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				canMove = true;
				return false;
			}
		});
		floatButton.setOnTouchListener(new MyOnTouchListener());
		floatButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onButtonClick();
			}
		});
		// 显示myFloatView图像
		wm.addView(myFV, wmParams);
	}

	class MyOnTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// 获取相对屏幕的坐标，即以屏幕左上角为原点
			x = event.getRawX();
			y = event.getRawY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 获取相对View的坐标，即以此View左上角为原点
				mTouchStartX = event.getX();
				mTouchStartY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				if(canMove){
					updateViewPosition();
				}
				break;
			case MotionEvent.ACTION_UP:
				if(canMove){
					updateViewPosition();
				}
				mTouchStartX = mTouchStartY = 0;
				break;
			}
			return false;
		}
	}

	private void onButtonClick() {
		if(canMove){
			canMove = false;
		}else{
			if (isOn) {
				// TODO 关闭
				CommandUtil.RootCommand("setprop service.adb.tcp.port -1" + "\n"
						+ "stop adbd" + "\n" + "start adbd");
				floatButton.setText("Wifi调试已关闭");
				isOn = false;
			} else {
				// TODO 开启
				CommandUtil.RootCommand("setprop service.adb.tcp.port 5555" + "\n"
						+ "stop adbd" + "\n" + "start adbd");
				floatButton.setText("Wifi调试已开启");
				isOn = true;
			}
		}
	}

	private void updateViewPosition() {
		// 更新浮动窗口位置参数
		wmParams.x = (int) (x - mTouchStartX);
		wmParams.y = (int) (y - mTouchStartY);
		wm.updateViewLayout(myFV, wmParams);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 在程序退出(Activity销毁）时销毁悬浮窗口
		wm.removeView(myFV);
	}

	public void switchButton(View v) {
		if (!hasFloatView) {
			// 显示浮动窗口
			createView();
			hasFloatView = true;
		} else {
			// 关闭浮动窗口
			wm.removeView(myFV);
			hasFloatView = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}