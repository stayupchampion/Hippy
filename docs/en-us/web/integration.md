# Web 集成
# Web Integration

这篇教程，讲述了如何将 Hippy 集成到 Web 页面中。
This tutorial shows how to integrate Hippy into a Web page.

> 不同于 @hippy/react-web 和 @hippy/vue-web 方案，本方案（Web Renderer）不会替换 @hippy/react 和 @hippy/vue，而是将运行在原生环境下的 bundle 原封不动运行到 Web 上，与转译 Web 的方案各有利弊，业务可根据具体场景采用合适的方案
> Different from @hippy/react-web and @hippy/vue-Web, this solution (Web Renderer) will not replace @hippy/react and @hippy/vue. Instead, the bundle running in the native environment will run intact on the Web, which has advantages and disadvantages with Web translation solution. Application can adopt appropriate solution according to specific scenarios

## 前期准备
## Preparation

- 模板文件：Web 运行需要一个 HTML 文件作为入口
- Template file: An HTML file is required as an entry for web working
- 入口文件：WebRenderer 是作为 Hippy bundle 的一个运行环境，因此不共享入口 JS 文件，应为其创建独立的入口文件
- Entry file: WebRenderer is a running environment of the Hippy bundle, so it does not share the JS entry file, and should create an independent entry file for it

## Demo 体验
## Experience the demo

若想快速体验，可以直接基于我们的 [HippyReact Web Demo](https://github.com/Tencent/Hippy/tree/master/examples/hippy-react-demo) 和 
[HippyVue Web Demo](https://github.com/Tencent/Hippy/tree/master/examples/hippy-vue-demo) 来体验
For A quick experience, you can base it directly on our [HippyReact Web Demo](https://github.com/Tencent/Hippy/tree/master/examples/hippy-react-demo) and [HippyVue Web Demo](https://github.com/Tencent/Hippy/tree/master/examples/hippy-vue-demo)

### npm script

在 demo 项目中，通过 `web:dev` 命令启动 WebRenderer 调试服务，通过 `web:build` 打包编译。
In the Demo project, run the `web:dev` command to start the WebRenderer debugging service, and run the `web:build` to package.

```json
  "scripts": {
    "web:dev": "npm run hippy:dev & cross-env-os os=\"Windows_NT,Linux\" minVersion=17 NODE_OPTIONS=--openssl-legacy-provider webpack serve --config ./scripts/hippy-webpack.web-renderer.dev.js",
    "web:build": "cross-env-os os=\"Windows_NT,Linux\" minVersion=17 NODE_OPTIONS=--openssl-legacy-provider webpack --config ./scripts/hippy-webpack.web-renderer.js"
  }
```

### 启动调试
### Start debugging

执行 `npm run web:dev` 启动 WebRenderer 调试，根据 demo 的 webpack 配置，WebRenderer 的 web 服务运行在`3000`端口，浏览器通过 `http://localhost:3000` 访问页面。
Run `npm run web:dev` to start WebRenderer debugging. According to the Webpack configuration of the demo, the WebRenderer web service runs on port `3000`, the browser accesses the page through `http://localhost:3000`.

## 快速接入
## Quick access

WebRenderer 的执行应符合以下流程：
The execution of WebRenderer shall comply with the following process:

1. 导入 WebRenderer：该阶段会初始化 Hippy 代码运行的环境
2. 加载业务 bundle：这个 bundle 与 Native 侧运行的 bundle 包保持一致
3. 启动 WebRenderer：该阶段会加载 Hippy 内置组件和模块，也可以加载自定义组件和模块

1. Import the WebRenderer: This stage will initialize the environment for the Hippy code to run
2. Load the application bundle: This bundle is consistent with the bundle package running on the native side
3. Start WebRenderer: This stage will load hippy built-in components and modules, or custom components and modules

### 导入 WebRenderer
### Import WebRenderer

#### 以 CDN 方式使用
#### Use in CDN mode

在模板文件内添加：
Add to the template file:

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" />
    <title>Example</title>
  </head>
  <body>
    <div id="root"></div>
    <!-- web renderer cdn url -->
    <!-- Hippy不提供cdn资源管理，需业务自行上传之类 -->
    <!-- Hippy does not provide CDN resource management and needs to be uploaded by the application itself -->
    <script src="//xxx.com/lib/hippy-web-renderer/0.1.1/hippy-web-renderer.js"></script>
    <script src="src/index.ts"></script>
  </body>
</html>
```

#### 以 NPM 包方式使用
#### Use in NPM package mode

```shell
npm install -S @hippy/web-renderer
```

在入口文件内添加：
Add to the entry file:

```javascript
// 1. import web renderer
import { HippyWebEngine, HippyWebModule } from '@hippy/web-renderer';

// 2. 导入业务 bundle 的入口文件，需放在 web renderer 导入之后
// 2. Import the entry file of the application bundle after the web renderer import

// 3. 创建 web engine，如果有业务自定义模块和组件，从此处传入
// 3. Create the web engine. If there are application custom modules and components, pass them in from here
```

### 加载业务 Bundle
### Load application bundle

加载 bundle 包有多种方式，可根据业务需要灵活选择，只需要确保引入顺序在 WebRenderer 之后即可
There are multiple ways to load the bundle package, which can be flexibly selected according to application needs. Just ensure that the import order is after the WebRenderer

#### 在模板文件内引用加载
#### Reference and load in the template file

```html
<script src="//xxx.com/lib/hippy-web-renderer/0.1.1/hippy-web-renderer.js"></script>
<!-- application bundle -->
<script src="//xxx.com/hippy-biz/index.bundle.js"></script>
<!-- entry file -->
<script src="src/index.ts"></script>
```

#### 在入口文件内动态加载
#### Load dynamically in the entry file

```javascript
import { HippyWebEngine } from '@hippy/web-renderer';

const engine = HippyWebEngine.create();

 engine.load('https://xxxx.com/hippy-bundle/index.bundle.js').then(() => {
  engine.start({
    id: 'root',
    name: 'example',
  });
});
```

#### 业务源码直接引用
#### Application source code direct reference

```javascript
import { HippyCallBack, HippyWebEngine, HippyWebModule, View } from '@hippy/web-renderer';
// 导入业务 bundle 的入口文件，需放在 web renderer 导入之后
// Import the entry file of the application bundle after the web renderer import
import './main';


const engine = HippyWebEngine.create();
```

### 启动 WebRenderer
### Start the WebRenderer

加载完业务 bundle 后，调用相关 API 创建并启动 WebRenderer
After the application bundle is loaded, the relevant API is invoked to create and start the WebRenderer

```js
// 创建 web engine，如果有业务自定义模块和组件，从此处传入
// Create the web engine. If there are application custom modules and components, pass them in from here
// 如果只使用官方模块和组件，则直接使用 const engine = HippyWebEngine.create() 即可
// If only official modules and components are used, directly use const engine = hippywebengine Create()
const engine = HippyWebEngine.create({
  modules: {
    CustomCommonModule,
  },
  components: {
    CustomPageView,
  },
});

// 启动 web renderer
// Start the web renderer
engine.start({
  // 挂载的 dom id
  // Mounted dom id
  id: 'root',
  // 模块名
  // Module name
  name: 'module-name',
  // 模块启动参数，业务自定义,
  // hippy-react 可以从 入口文件props里获取，hippy-vue可以从 app.$options.$superProps 里获取
  // The module startup parameters are customized by the service,
  // hippy-react can be obtained from the props entry file, and hippy-vue can be obtained from app.$options.$superProps
  params: {
    path: '/home',
    singleModule: true,
    isSingleMode: true,
    business: '',
    data: { },
  },
});
```
