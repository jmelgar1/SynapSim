import { ref, watch } from 'vue'

/**
 * Composable for reactive localStorage management
 * @param {string} key - localStorage key
 * @param {*} defaultValue - default value if key doesn't exist
 * @returns {Object} - reactive ref and save function
 */
export function useLocalStorage(key, defaultValue = null) {
  const data = ref(defaultValue)

  // Load initial value from localStorage
  const loadFromStorage = () => {
    try {
      const stored = localStorage.getItem(key)
      if (stored) {
        data.value = JSON.parse(stored)
      }
    } catch (error) {
      console.error(`Error loading from localStorage (${key}):`, error)
    }
  }

  // Save to localStorage
  const saveToStorage = (value) => {
    try {
      localStorage.setItem(key, JSON.stringify(value))
      data.value = value
    } catch (error) {
      console.error(`Error saving to localStorage (${key}):`, error)
    }
  }

  // Remove from localStorage
  const removeFromStorage = () => {
    try {
      localStorage.removeItem(key)
      data.value = defaultValue
    } catch (error) {
      console.error(`Error removing from localStorage (${key}):`, error)
    }
  }

  // Load initial data
  loadFromStorage()

  // Watch for changes and auto-save (optional, can be disabled)
  const enableAutoSave = () => {
    watch(
      data,
      (newValue) => {
        if (newValue !== null && newValue !== undefined) {
          saveToStorage(newValue)
        }
      },
      { deep: true }
    )
  }

  return {
    data,
    save: saveToStorage,
    remove: removeFromStorage,
    enableAutoSave,
  }
}
