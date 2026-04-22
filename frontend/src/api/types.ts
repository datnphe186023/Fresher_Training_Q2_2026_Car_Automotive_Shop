export interface ServiceResponse {
  id: number
  name: string
  category: { id: number; name: string }
  description: string
  basePrice: number
  durationMinutes: number
  imageUrls: string[]
}

export interface BookingResponse {
  id: number
  bookingReference: string
  customer: { id: number; phoneNumber: string; name: string }
  service: ServiceResponse
  bookingDate: string
  status: 'PENDING' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: { id: number; username: string; email: string; role: string }
}

export interface CreateBookingRequest {
  phoneNumber: string
  email?: string
  name?: string
  serviceId: number
  bookingDate: string
}

export interface LoginRequest {
  username: string
  password: string
}
