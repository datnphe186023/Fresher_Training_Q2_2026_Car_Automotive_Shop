import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { TOKEN_KEY } from './api/axiosInstance'
import ProtectedRoute from './components/ProtectedRoute'
import HomePage from './pages/HomePage'
import BookingPage from './pages/BookingPage'
import TrackingPage from './pages/TrackingPage'
import LoginPage from './pages/LoginPage'
import AdminDashboard from './pages/AdminDashboard'
import NotFoundPage from './pages/NotFoundPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/booking" element={<BookingPage />} />
        <Route path="/tracking" element={<TrackingPage />} />
        <Route
          path="/login"
          element={
            localStorage.getItem(TOKEN_KEY)
              ? <Navigate to="/admin" replace />
              : <LoginPage />
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
