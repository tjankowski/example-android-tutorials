package my.todolist;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

public class ToDoEditText extends EditText {

	private Paint marginPaint;
	private Paint linePaint;
	private int paperColor;
	private float margin;

	public ToDoEditText(Context context) {
		super(context);
		init();
	}
	
	public ToDoEditText(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		init();
	}
	
	public ToDoEditText(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
		init();
	}
	
	private void init() {
		Resources myResources = getResources();
		
		marginPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		marginPaint.setColor(myResources.getColor(R.color.notepad_margin));
		
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setColor(myResources.getColor(R.color.notepad_lines));
		
		paperColor = myResources.getColor(R.color.notepad_paper);
		margin = myResources.getDimension(R.dimen.notepad_margin);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(paperColor);
		
		canvas.drawLine(0, 0, getMeasuredHeight(), 0, linePaint);
		canvas.drawLine(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight(), linePaint);
		
		canvas.drawLine(margin, 0, margin, getMeasuredWidth(), marginPaint);
		
		canvas.save();
		canvas.translate(margin, 0);
		
		super.onDraw(canvas);
		canvas.restore();
	}

	
}
