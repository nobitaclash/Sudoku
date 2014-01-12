package pis.android.sudoku.widget;

import pis.android.sudoku.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class ButtonWithFont extends Button {

	public ButtonWithFont(Context context) {
		super(context);
	}

	public ButtonWithFont(Context context, AttributeSet attrs) {
		super(context, attrs, android.R.attr.buttonStyle);
		customFont(context, attrs);
	}

	public ButtonWithFont(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		customFont(context, attrs);
	}

	public void customFont(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ViewWithFont, android.R.attr.buttonStyle, 0);
//		String font = a.getString(R.styleable.ViewWithFont_textFont);
		Typeface typeface = Typeface.createFromAsset(context.getAssets(),
				"fonts/DroidSans.ttf");
		this.setTypeface(typeface);
		a.recycle();
	}
}
