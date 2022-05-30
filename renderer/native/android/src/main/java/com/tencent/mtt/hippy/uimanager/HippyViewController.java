/* Tencent is pleased to support the open source community by making Hippy available.
 * Copyright (C) 2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.mtt.hippy.uimanager;

import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.os.MessageQueue;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tencent.link_supplier.proxy.renderer.Renderer;
import com.tencent.mtt.hippy.annotation.HippyControllerProps;
import com.tencent.mtt.hippy.common.HippyArray;
import com.tencent.mtt.hippy.common.HippyMap;
import com.tencent.mtt.hippy.dom.node.NodeProps;
import com.tencent.mtt.hippy.modules.Promise;
import com.tencent.mtt.hippy.utils.DevtoolsUtil;
import com.tencent.mtt.hippy.utils.LogUtils;
import com.tencent.mtt.hippy.utils.PixelUtil;
import com.tencent.mtt.hippy.views.common.CommonBorder;
import com.tencent.mtt.hippy.views.view.HippyViewGroupController;
import com.tencent.mtt.supportui.views.IGradient;
import com.tencent.mtt.supportui.views.IShadow;
import com.tencent.renderer.NativeRender;
import com.tencent.renderer.NativeRenderContext;
import com.tencent.renderer.NativeRendererManager;
import com.tencent.renderer.component.text.VirtualNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"deprecation", "unused"})
public abstract class HippyViewController<T extends View & HippyViewBase> implements
        View.OnFocusChangeListener {

    private static final String TAG = "HippyViewController";

    private static final MatrixUtil.MatrixDecompositionContext sMatrixDecompositionContext = new MatrixUtil.MatrixDecompositionContext();
    private static final double[] sTransformDecompositionArray = new double[16];
    private boolean bUserChangeFocus = false;

    @SuppressWarnings("deprecation")
    public View createView(@Nullable ViewGroup rootView, int id, @NonNull Renderer renderer,
            @NonNull String className, @Nullable Map<String, Object> props) {
        View view = null;
        if (rootView != null) {
            Context context = rootView.getContext();
            Object object = renderer.getCustomViewCreator();
            if (object instanceof HippyCustomViewCreator) {
                view = ((HippyCustomViewCreator) object)
                        .createCustomView(className, context, props);
            }
            if (view == null) {
                view = createViewImpl(context, props);
                if (view == null) {
                    view = createViewImpl(context);
                }
            }
            view.setId(id);
            Map<String, Object> tagMap = NativeViewTag.createViewTag(className);
            view.setTag(tagMap);
        }
        return view;
    }

    public void onAfterUpdateProps(T v) {

    }

    protected void updateExtra(View view, @Nullable Object object) {

    }

    public void updateLayout(int id, int x, int y, int width, int height,
            ControllerRegistry componentHolder) {
        View view = componentHolder.getView(id);
        if (view != null) {
            view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            if (!shouldInterceptLayout(view, x, y, width, height)) {
                view.layout(x, y, x + width, y + height);
            }
        }
    }

    protected boolean shouldInterceptLayout(View view, int x, int y, int width, int height) {
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean handleGestureBySelf() {
        return false;
    }

    @Deprecated
    protected abstract View createViewImpl(Context context);

    protected View createViewImpl(@NonNull Context context, @Nullable Map<String, Object> props) {
        return null;
    }


    /**
     * transform
     **/
    @HippyControllerProps(name = NodeProps.TRANSFORM, defaultType = HippyControllerProps.ARRAY)
    public void setTransform(T view, ArrayList transformArray) {
        if (transformArray == null) {
            resetTransform(view);
        } else {
            applyTransform(view, transformArray);
        }
    }

    @HippyControllerProps(name = NodeProps.PROP_ACCESSIBILITY_LABEL)
    public void setAccessibilityLabel(T view, String accessibilityLabel) {
        if (accessibilityLabel == null) {
            accessibilityLabel = "";
        }
        view.setContentDescription(accessibilityLabel);
    }

    /**
     * zIndex
     **/
    @HippyControllerProps(name = NodeProps.Z_INDEX, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setZIndex(T view, int zIndex) {
        HippyViewGroupController.setViewZIndex(view, zIndex);
        ViewParent parent = view.getParent();
        if (parent instanceof IHippyZIndexViewGroup) {
            ((IHippyZIndexViewGroup) parent).updateDrawingOrder();
        }
    }

    /**
     * color/border/alpha
     **/
    @HippyControllerProps(name = NodeProps.BACKGROUND_COLOR, defaultType = HippyControllerProps.NUMBER, defaultNumber = Color.TRANSPARENT)
    public void setBackground(T view, int backgroundColor) {
        view.setBackgroundColor(backgroundColor);
    }

    @HippyControllerProps(name = NodeProps.OPACITY, defaultType = HippyControllerProps.NUMBER, defaultNumber = 1.f)
    public void setOpacity(T view, float opacity) {
        view.setAlpha(opacity);
    }

    @HippyControllerProps(name = NodeProps.BORDER_RADIUS, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setBorderRadius(T view, float borderRadius) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderRadius(borderRadius,
                            CommonBorder.BorderRadiusDirection.ALL.ordinal());
        }
    }


    @HippyControllerProps(name = NodeProps.BORDER_TOP_LEFT_RADIUS, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setTopLeftBorderRadius(T view, float topLeftBorderRadius) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view).setBorderRadius(topLeftBorderRadius,
                    CommonBorder.BorderRadiusDirection.TOP_LEFT.ordinal());
        }
    }

    @HippyControllerProps(name = NodeProps.BORDER_TOP_RIGHT_RADIUS, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setTopRightBorderRadius(T view, float topRightBorderRadius) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view).setBorderRadius(topRightBorderRadius,
                    CommonBorder.BorderRadiusDirection.TOP_RIGHT.ordinal());
        }
    }

    @HippyControllerProps(name = NodeProps.BORDER_BOTTOM_RIGHT_RADIUS, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setBottomRightBorderRadius(T view, float bottomRightBorderRadius) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view).setBorderRadius(bottomRightBorderRadius,
                    CommonBorder.BorderRadiusDirection.BOTTOM_RIGHT.ordinal());
        }
    }

    @HippyControllerProps(name = NodeProps.BORDER_BOTTOM_LEFT_RADIUS, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setBottomLeftBorderRadius(T view, float bottomLeftBorderRadius) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view).setBorderRadius(bottomLeftBorderRadius,
                    CommonBorder.BorderRadiusDirection.BOTTOM_LEFT.ordinal());
        }
    }

    @HippyControllerProps(name = NodeProps.BORDER_WIDTH, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setBorderWidth(T view, float borderWidth) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderWidth(borderWidth, CommonBorder.BorderWidthDirection.ALL.ordinal());
        }
    }

    @HippyControllerProps(name = NodeProps.NEXT_FOCUS_DOWN_ID, defaultType = HippyControllerProps.BOOLEAN)
    public void setNextFocusDownId(T view, int id) {
        view.setNextFocusDownId(id);
    }

    @HippyControllerProps(name = NodeProps.NEXT_FOCUS_UP_ID, defaultType = HippyControllerProps.BOOLEAN)
    public void setNextFocusUpId(T view, int id) {
        view.setNextFocusUpId(id);
    }

    @HippyControllerProps(name = NodeProps.NEXT_FOCUS_LEFT_ID, defaultType = HippyControllerProps.BOOLEAN)
    public void setNextFocusLeftId(T view, int id) {
        view.setNextFocusLeftId(id);
    }

    @HippyControllerProps(name = NodeProps.NEXT_FOCUS_RIGHT_ID, defaultType = HippyControllerProps.BOOLEAN)
    public void setNextFocusRightId(T view, int id) {
        view.setNextFocusRightId(id);
    }

    @HippyControllerProps(name = NodeProps.FOCUSABLE, defaultType = HippyControllerProps.BOOLEAN)
    public void setFocusable(T view, boolean focusable) {
        view.setFocusable(focusable);
        if (focusable) {
            view.setOnFocusChangeListener(this);
        } else {
            view.setOnFocusChangeListener(null);
        }
    }

    @HippyControllerProps(name = NodeProps.REQUEST_FOCUS, defaultType = HippyControllerProps.BOOLEAN)
    public void requestFocus(final T view, boolean request) {
        if (request) {
            //noinspection AccessStaticViaInstance
            Looper.getMainLooper().myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                @Override
                public boolean queueIdle() {
                    bUserChangeFocus = true;
                    boolean result = view.requestFocusFromTouch();

                    if (!result) {
                        result = view.requestFocus();
                        LogUtils.d("requestFocus", "requestFocus result:" + result);
                    }
                    bUserChangeFocus = false;
                    return false;
                }
            });

        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (bUserChangeFocus) {
            HippyMap hippyMap = new HippyMap();
            hippyMap.pushBoolean("focus", hasFocus);
            new HippyViewEvent("onFocus").send(v, hippyMap);
        }
    }

    @HippyControllerProps(name = NodeProps.LINEAR_GRADIENT, defaultType = HippyControllerProps.MAP)
    public void setLinearGradient(T view, HippyMap linearGradient) {
        if (linearGradient != null && view instanceof IGradient) {
            String angle = linearGradient.getString("angle");
            HippyArray colorStopList = linearGradient.getArray("colorStopList");

            if (TextUtils.isEmpty(angle) || colorStopList == null || colorStopList.size() == 0) {
                return;
            }

            int size = colorStopList.size();
            ArrayList<Integer> colorsArray = new ArrayList<>();
            ArrayList<Float> positionsArray = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                HippyMap colorStop = colorStopList.getMap(i);
                if (colorStop == null) {
                    continue;
                }

                int color = colorStop.getInt("color");
                colorsArray.add(color);

                float ratio = 0.0f;
                if (colorStop.containsKey("ratio")) {
                    ratio = (float) colorStop.getDouble("ratio");
                } else if (i == (size - 1)) {
                    ratio = 1.0f;
                }

                positionsArray.add(ratio);
            }

            ((IGradient) view).setGradientAngle(angle);
            ((IGradient) view).setGradientColors(colorsArray);
            ((IGradient) view).setGradientPositions(positionsArray);
        }
    }

    @HippyControllerProps(name = NodeProps.SHADOW_OFFSET, defaultType = HippyControllerProps.MAP)
    public void setShadowOffset(T view, HippyMap shadowOffset) {
        if (shadowOffset != null && view instanceof IShadow) {
            float shadowOffsetX = shadowOffset.getInt("x");
            float shadowOffsetY = shadowOffset.getInt("y");
            ((IShadow) view).setShadowOffsetX(shadowOffsetX);
            ((IShadow) view).setShadowOffsetY(shadowOffsetY);
        }
    }

    @HippyControllerProps(name = NodeProps.SHADOW_OFFSET_X, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setShadowOffsetX(T view, float shadowOffsetX) {
        if (view instanceof IShadow) {
            ((IShadow) view).setShadowOffsetX(shadowOffsetX);
        }
    }

    @HippyControllerProps(name = NodeProps.SHADOW_OFFSET_Y, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setShadowOffsetY(T view, float shadowOffsetY) {
        if (view instanceof IShadow) {
            ((IShadow) view).setShadowOffsetY(shadowOffsetY);
        }
    }

    @HippyControllerProps(name = NodeProps.SHADOW_OPACITY, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setShadowOpacity(T view, float shadowOpacity) {
        if (view instanceof IShadow) {
            ((IShadow) view).setShadowOpacity(shadowOpacity);
        }
    }

    @HippyControllerProps(name = NodeProps.SHADOW_RADIUS, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setShadowRadius(T view, float shadowRadius) {
        if (view instanceof IShadow) {
            ((IShadow) view).setShadowRadius(shadowRadius);
        }
    }

    @HippyControllerProps(name = NodeProps.SHADOW_SPREAD, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setShadowSpread(T view, float shadowSpread) {
        if (view instanceof IShadow) {
            ((IShadow) view).setShadowSpread(shadowSpread);
        }
    }

    @HippyControllerProps(name = NodeProps.SHADOW_COLOR, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setShadowColor(T view, int shadowColor) {
        if (view instanceof IShadow) {
            ((IShadow) view).setShadowColor(shadowColor);
        }
    }

    @HippyControllerProps(name = NodeProps.BORDER_LEFT_WIDTH, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setLeftBorderWidth(T view, float borderLeftWidth) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderWidth(borderLeftWidth,
                            CommonBorder.BorderWidthDirection.LEFT.ordinal());
        }
    }


    @HippyControllerProps(name = NodeProps.BORDER_TOP_WIDTH, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setTopBorderWidth(T view, float borderTopWidth) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderWidth(borderTopWidth,
                            CommonBorder.BorderWidthDirection.TOP.ordinal());
        }
    }

    @HippyControllerProps(name = NodeProps.BORDER_RIGHT_WIDTH, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setRightBorderWidth(T view, float borderRightWidth) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderWidth(borderRightWidth,
                            CommonBorder.BorderWidthDirection.RIGHT.ordinal());
        }
    }

    @HippyControllerProps(name = NodeProps.BORDER_BOTTOM_WIDTH, defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setBottomBorderWidth(T view, float borderBottomWidth) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderWidth(borderBottomWidth,
                            CommonBorder.BorderWidthDirection.BOTTOM.ordinal());
        }
    }

    @HippyControllerProps(name = NodeProps.BORDER_COLOR, defaultType = HippyControllerProps.NUMBER, defaultNumber = Color.TRANSPARENT)
    public void setBorderColor(T view, int borderColor) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderColor(borderColor, CommonBorder.BorderWidthDirection.ALL.ordinal());
        }
    }

    @HippyControllerProps(name = NodeProps.BORDER_LEFT_COLOR, defaultType = HippyControllerProps.NUMBER, defaultNumber = Color.TRANSPARENT)
    public void setBorderLeftColor(T view, int borderLeftColor) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderColor(borderLeftColor,
                            CommonBorder.BorderWidthDirection.LEFT.ordinal());
        }
    }


    @HippyControllerProps(name = NodeProps.BORDER_TOP_COLOR, defaultType = HippyControllerProps.NUMBER, defaultNumber = Color.TRANSPARENT)
    public void setBorderTopWidth(T view, int borderTopColor) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderColor(borderTopColor,
                            CommonBorder.BorderWidthDirection.TOP.ordinal());
        }
    }

    @HippyControllerProps(name = NodeProps.BORDER_RIGHT_COLOR, defaultType = HippyControllerProps.NUMBER, defaultNumber = Color.TRANSPARENT)
    public void setBorderRightWidth(T view, int borderRightColor) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderColor(borderRightColor,
                            CommonBorder.BorderWidthDirection.RIGHT.ordinal());
        }
    }


    @HippyControllerProps(name = NodeProps.BORDER_BOTTOM_COLOR, defaultType = HippyControllerProps.NUMBER, defaultNumber = Color.TRANSPARENT)
    public void setBorderBottomWidth(T view, int borderBottomColor) {
        if (view instanceof CommonBorder) {
            ((CommonBorder) view)
                    .setBorderColor(borderBottomColor,
                            CommonBorder.BorderWidthDirection.BOTTOM.ordinal());
        }
    }

    /**
     * touch/click
     **/
    @HippyControllerProps(name = NodeProps.ON_CLICK, defaultType = HippyControllerProps.BOOLEAN)
    public void setClickable(T view, boolean flag) {
        if (!handleGestureBySelf()) {
            if (flag) {
                view.setOnClickListener(NativeGestureDispatcher.getOnClickListener());
            } else {
                view.setOnClickListener(null);
                view.setClickable(false);
            }
        }
    }


    @HippyControllerProps(name = NodeProps.ON_LONG_CLICK, defaultType = HippyControllerProps.BOOLEAN)
    public void setLongClickable(T view, boolean flag) {
        if (!handleGestureBySelf()) {
            if (flag) {
                view.setOnLongClickListener(NativeGestureDispatcher.getOnLongClickListener());
            } else {
                view.setOnLongClickListener(null);
                view.setLongClickable(false);
            }
        }
    }

    @HippyControllerProps(name = NodeProps.ON_PRESS_IN, defaultType = HippyControllerProps.BOOLEAN)
    public void setPressInable(T view, boolean flag) {
        if (!handleGestureBySelf()) {
            setGestureType(view, NodeProps.ON_PRESS_IN, flag);
        }
    }

    @HippyControllerProps(name = NodeProps.ON_PRESS_OUT, defaultType = HippyControllerProps.BOOLEAN)
    public void setPressOutable(T view, boolean flag) {
        if (!handleGestureBySelf()) {
            setGestureType(view, NodeProps.ON_PRESS_OUT, flag);
        }
    }

    @HippyControllerProps(name = NodeProps.ON_TOUCH_DOWN, defaultType = HippyControllerProps.BOOLEAN)
    public void setTouchDownHandle(T view, boolean flag) {
        if (!handleGestureBySelf()) {
            setGestureType(view, NodeProps.ON_TOUCH_DOWN, flag);
        }
    }

    @HippyControllerProps(name = NodeProps.ON_TOUCH_MOVE, defaultType = HippyControllerProps.BOOLEAN)
    public void setTouchMoveHandle(T view, boolean flag) {
        if (!handleGestureBySelf()) {
            setGestureType(view, NodeProps.ON_TOUCH_MOVE, flag);
        }
    }

    @HippyControllerProps(name = NodeProps.ON_TOUCH_END, defaultType = HippyControllerProps.BOOLEAN)
    public void setTouchEndHandle(T view, boolean flag) {
        if (!handleGestureBySelf()) {
            setGestureType(view, NodeProps.ON_TOUCH_END, flag);
        }
    }

    @HippyControllerProps(name = NodeProps.ON_TOUCH_CANCEL, defaultType = HippyControllerProps.BOOLEAN)
    public void setTouchCancelHandle(T view, boolean flag) {
        if (!handleGestureBySelf()) {
            setGestureType(view, NodeProps.ON_TOUCH_CANCEL, flag);
        }
    }

    @HippyControllerProps(name = NodeProps.ON_ATTACHED_TO_WINDOW, defaultType = HippyControllerProps.BOOLEAN)
    public void setAttachedToWindowHandle(T view, boolean flag) {
        if (flag) {
            view.addOnAttachStateChangeListener(
                    NativeGestureDispatcher.getOnAttachedToWindowListener());
        } else {
            view.removeOnAttachStateChangeListener(
                    NativeGestureDispatcher.getOnAttachedToWindowListener());
        }
    }

    @HippyControllerProps(name = NodeProps.ON_DETACHED_FROM_WINDOW, defaultType = HippyControllerProps.BOOLEAN)
    public void setDetachedFromWindowHandle(T view, boolean flag) {
        if (flag) {
            view.addOnAttachStateChangeListener(
                    NativeGestureDispatcher.getOnDetachedFromWindowListener());
        } else {
            view.removeOnAttachStateChangeListener(
                    NativeGestureDispatcher.getOnDetachedFromWindowListener());
        }
    }

    @HippyControllerProps(name = "renderToHardwareTextureAndroid", defaultType = HippyControllerProps.BOOLEAN)
    public void setRenderToHardwareTexture(T view, boolean useHWTexture) {
        view.setLayerType(useHWTexture ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE, null);
    }

    @SuppressWarnings("EmptyMethod")
    @HippyControllerProps(name = NodeProps.CUSTOM_PROP)
    public void setCustomProp(T view, String methodName, Object props) {

    }

    protected void setGestureType(T view, String type, boolean flag) {
        if (flag) {
            if (view.getGestureDispatcher() == null) {
                view.setGestureDispatcher(new NativeGestureDispatcher(view));
            }
            view.getGestureDispatcher().addGestureType(type);
        } else {
            if (view.getGestureDispatcher() != null) {
                view.getGestureDispatcher().removeGestureType(type);
            }
        }
    }

    public RenderNode createRenderNode(int id, @Nullable Map<String, Object> props,
            @NonNull String className, @NonNull ViewGroup hippyRootView,
            @NonNull ControllerManager controllerManager, boolean isLazy) {
        return new RenderNode(id, props, className, hippyRootView, controllerManager, isLazy);
    }

    @Nullable
    public VirtualNode createVirtualNode(int rootId, int id, int pid, int index,
            @Nullable Map<String, Object> props) {
        // The host can create customize virtual node in a derived class.
        return null;
    }

    private void applyTransform(T view, ArrayList<Object> transformArray) {
        TransformUtil.processTransform(transformArray, sTransformDecompositionArray);
        sMatrixDecompositionContext.reset();
        MatrixUtil.decomposeMatrix(sTransformDecompositionArray, sMatrixDecompositionContext);
        view.setTranslationX(PixelUtil.dp2px((float) sMatrixDecompositionContext.translation[0]));
        view.setTranslationY(PixelUtil.dp2px((float) sMatrixDecompositionContext.translation[1]));
        view.setRotation((float) sMatrixDecompositionContext.rotationDegrees[2]);
        view.setRotationX((float) sMatrixDecompositionContext.rotationDegrees[0]);
        view.setRotationY((float) sMatrixDecompositionContext.rotationDegrees[1]);
        view.setScaleX((float) sMatrixDecompositionContext.scale[0]);
        view.setScaleY((float) sMatrixDecompositionContext.scale[1]);
    }

    public static void resetTransform(View view) {
        view.setTranslationX(0);
        view.setTranslationY(0);
        view.setRotation(0);
        view.setRotationX(0);
        view.setRotationY(0);
        view.setScaleX(1);
        view.setScaleY(1);
    }

    @Deprecated
    public void dispatchFunction(@NonNull T view, @NonNull String functionName,
            @NonNull HippyArray params) {
    }

    @Deprecated
    public void dispatchFunction(@NonNull T view, @NonNull String functionName,
            @NonNull HippyArray params, @NonNull Promise promise) {
    }

    public void dispatchFunction(@NonNull T view, @NonNull String functionName,
            @NonNull List params) {
    }

    public void dispatchFunction(@NonNull T view, @NonNull String functionName,
            @NonNull List params, @NonNull Promise promise) {
        switch (functionName) {
            case DevtoolsUtil.GET_SCREEN_SHOT:
                DevtoolsUtil.getScreenShot(view, promise);
                break;
            case DevtoolsUtil.ADD_FRAME_CALLBACK:
                DevtoolsUtil.addFrameCallback(params, view, promise);
                break;
            case DevtoolsUtil.REMOVE_FRAME_CALLBACK:
                DevtoolsUtil.removeFrameCallback(params, view, promise);
                break;
            default:
                break;
        }
    }

    public void onBatchComplete(@NonNull T view) {

    }

    public void onBatchStart(@NonNull T view) {

    }

    public void onViewDestroy(T t) {

    }

    protected void deleteChild(ViewGroup parentView, View childView) {
        parentView.removeView(childView);
    }

    protected void deleteChild(ViewGroup parentView, View childView, int childIndex) {
        deleteChild(parentView, childView);
    }

    protected void addView(ViewGroup parentView, View view, int index) {
        int realIndex = index;
        if (realIndex > parentView.getChildCount()) {
            realIndex = parentView.getChildCount();
        }
        try {
            parentView.addView(view, realIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getChildCount(T viewGroup) {
        if (viewGroup instanceof ViewGroup) {
            return ((ViewGroup) viewGroup).getChildCount();
        }
        return 0;
    }

    public View getChildAt(T viewGroup, int i) {
        if (viewGroup instanceof ViewGroup) {
            return ((ViewGroup) viewGroup).getChildAt(i);
        }
        return null;
    }

    protected String getInnerPath(NativeRenderContext context, String path) {
        int instanceId = context.getInstanceId();
        NativeRender nativeRenderer = NativeRendererManager.getNativeRenderer(instanceId);

        //hpfile://./assets/file_banner02.jpg
        if (path != null && path.startsWith("hpfile://")) {
            String relativePath = path.replace("hpfile://./", "");
            String bundlePath = null;
            if (nativeRenderer != null) {
                bundlePath = nativeRenderer.getBundlePath();
            }

            path = bundlePath == null ? null
                    : bundlePath.subSequence(0, bundlePath.lastIndexOf(File.separator) + 1)
                            + relativePath;
            //assets://index.android.jsbundle
            //file:sdcard/hippy/feeds/index.android.jsbundle
        }
        return path;
    }
}
