/// <reference types="vite/client" />

/**
 * Vite environment variable type definitions.
 * Extend ImportMetaEnv to add project-specific env variables.
 * Only variables prefixed with VITE_ are exposed to the client side.
 */
interface ImportMetaEnv {
  /**
   * Fallback API base URL used when application.yml cannot be loaded.
   * Set this in agent/.env file.
   * Example: VITE_API_BASE_URL=http://localhost:8080
   */
  readonly VITE_API_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
