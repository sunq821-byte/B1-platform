import { createApp } from "vue"
import { createPinia } from "pinia"

import App from "./App.vue"
import router from "./router"
import "element-plus/dist/index.css"
import "./styles/design-tokens.css"
import "./styles/prototype.css"
import { setupMock } from "./mock"

async function bootstrap() {
  await setupMock()

  const app = createApp(App)

  app.use(createPinia())
  app.use(router)

  app.mount("#app")
}

bootstrap()
