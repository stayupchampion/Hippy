# Web Event

 当浏览器加载结束或者使用了一些三方库完成一些功能后需要向业务侧抛出事件。
 When the browser finishes loading or uses some third-party libraries to complete some functions, it needs to throw events to the application side.

# Send event

 WebRenderer 在需要发送事件的地方调用以下代码：
 The WebRenderer calls the following code where the event needs to be sent:

## 在模块中
## In the module

 发送事件，`HippyWebModule` 基类提供的 `context` 提供了向业务层发送事件的能力
 To send events, the `context` provided by the `HippyWebModule` base class provides the ability to send events to the application layer

```javascript
const eventName = 'CustomName';
const param = {};
context.sendEvent(eventName, param);
```

## 在 component 中
## In the component

 发送事件，`HippyWebView` 基类的 `context` 提供了向业务层发送事件的能力
 To send events, the `context` of the `HippyWebView` base class provides the ability to send events to the application layer

```javascript
const eventName = 'CustomName';
const param = {};
context.sendEvent(eventName, param);
```

## 在全局中
## In the global

 发送事件，`Hippy.web.engine` 的 `context` 提供了向业务层发送事件的能力
 To send events, the `context` of `Hippy.web.engine` provides the ability to send events to the application layer

```javascript
const engine = Hippy.web.engine;
const eventName = 'CustomName';
const param = {};
engine.context.sendEvent(eventName, param);
```

# 前端业务监听事件
# Front-end application listening events

[hippy-react 监听事件](hippy-react/native-event.md?id=事件监听器)
[hippy-react listening events](hippy-react/native-event.md?id=EventListener)

[hippy-vue 监听事件](hippy-vue/native-event.md?id=事件监听器)
[hippy-vue listening events](hippy-vue/native-event.md?id=EventListener)
