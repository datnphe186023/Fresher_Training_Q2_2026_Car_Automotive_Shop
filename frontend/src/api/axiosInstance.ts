import axios from 'axios'

export const TOKEN_KEY = 'carshop_token'

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
})

// Request interceptor — attach JWT
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor — handle 401
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default axiosInstance
