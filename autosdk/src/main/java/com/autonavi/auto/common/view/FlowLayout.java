package com.autonavi.auto.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {
    private static final String TAG = "FlowLayout";

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取测量模式，这里有三种模式
        //wrap_content-> MeasureSpec.AT_MOST
        //match_parent -> MeasureSpec.EXACTLY
        //具体值 -> MeasureSpec.EXACTLY
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //获取布局控件的宽高，这里我们设置的宽位match_parent所以宽位手机屏幕的宽，高我们设置的是wrap_content这里测量出来的
        //是手机屏幕的高，当然我们不会用这里的高，我们要实际子view个数算出高。
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);

        //定义每一行的宽，循环子view累加
        int lineWidth = 0;
        //定义每一行的高度即子view的高度
        int lineHeight = 0;
        //定义父容器的宽度，其实这里感觉没必要设置
        int width = 0;
        //这里下面循环子view出来的总共布局的高度
        int height = 0;
        //获取所有子view并循环
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            //获取view
            View childAt = getChildAt(i);
            //测量子view这里一定要先测量，再调用childAt.getMeasuredWidth()和childAt.getMeasuredHeight()
            //还有在onlayout结束后才可以调用getWidth()和getHeight()
            measureChild(childAt,widthMeasureSpec,heightMeasureSpec);
            //这里就取到了重写marginLayoutParams的那几个方法获取布局文件中的margin
            MarginLayoutParams lp = null;
            if(childAt.getLayoutParams() instanceof MarginLayoutParams){
                lp = (MarginLayoutParams) childAt.getLayoutParams();
            }else{
                lp  = new MarginLayoutParams(0,0);
            }
            //获取子view的宽+左右外边距
            int childWidth = childAt.getMeasuredWidth()+lp.leftMargin+lp.rightMargin;
            //获取子view的高+上下外边距
            int childHeight = childAt.getMeasuredHeight()+lp.topMargin+lp.bottomMargin;
            //一列中累加的宽度也就是linewidth+这次子view的宽度如果超过了， int measureWidth = 		   	                         //MeasureSpec.getSize(widthMeasureSpec);就让其换行。
            if(lineWidth+childWidth>measureWidth){
                //width就等于一列累加的宽度，这里如果布局文件中设置的是match_parent这里其实没啥用
                width = lineWidth;
                //行高累加
                height+=lineHeight;
                //将换行的的这个view的宽高，也就是下一行的第一个view赋值宽和高
                lineWidth = childWidth;
                lineHeight = childHeight;
            }else{
                //如果已将累加的列宽+本次的子view的宽不大于measureWidth那就宽度lineWidth+=childWidth;
                //高度的话取行高和本次子view中偏高的，由于我们布局文件中设置的子view高度都是一样的所有这一句也没什么用。
                lineHeight = Math.max(lineHeight,childHeight);
                lineWidth+=childWidth;
            }
            //最后一个元素，将行高+本次子view的高度。这个地方开始我也有点迷糊，最后还是断点一步步执行才明白过来，如果这里不判断是否是最后一行的话，循环完成后是少加了一次行高的，最后一行的view永远显示不出来。
            if (i == childCount -1){
                height += lineHeight;
                width = Math.max(width,lineWidth);
            }

        }
        //重新设置本父容器测量过后的结果，三元运算判断是哪种模式，用width还是measureWidth
        setMeasuredDimension((widthMode==MeasureSpec.EXACTLY)?measureWidth:width,(heightMode==MeasureSpec.EXACTLY)?measureHeight:height);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //还是先获取所有的子view
        int count = getChildCount();
        //定义列宽
        int lineWidth = 0;
        //定义行高
        int lineHeight = 0;
        //定义上、左边距
        int top = 0,left=0;
        for (int i = 0; i < count; i++) {
            View childAt = getChildAt(i);
            MarginLayoutParams layoutParams = (MarginLayoutParams) childAt.getLayoutParams();
            //因为onMeasure(int widthMeasureSpec, int heightMeasureSpec)方法已经执行完，所有这里我们可以直接调用
            //子view的宽+左右边距
            int childWidth = childAt.getMeasuredWidth()+layoutParams.leftMargin+layoutParams.rightMargin;
            int childHeight = childAt.getMeasuredHeight()+layoutParams.topMargin+layoutParams.bottomMargin;
            //这里的if判断和onMeasure中是一样的逻辑，不再赘述
            if(childWidth+lineWidth>getMeasuredWidth()){
                //累加top
                top+=lineHeight;
                //因为换行了left置为0
                left = 0;
                lineHeight = childHeight;
                lineWidth = childWidth;
            }else{
                lineHeight = Math.max(lineHeight,childHeight);
                //行宽累加
                lineWidth+=childWidth;
            }
            //计算子view的左、上、右、下的值
            int lc = left+layoutParams.leftMargin;
            int tc = top+layoutParams.topMargin;
            //右边就等于自己的宽+左边的边距即lc
            int rc = lc+childAt.getMeasuredWidth();
            //底部逻辑同上
            int bc = tc+childAt.getMeasuredHeight();
            //布局
            childAt.layout(lc,tc,rc,bc);
            //这一句很重要，因为一行中有多个view，所有left是累加的关系。
            left+=childWidth;
        }
    }

}
