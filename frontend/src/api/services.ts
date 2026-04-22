import axiosInstance from './axiosInstance'
import type {
  AuthResponse,
  BookingResponse,
  CreateBookingRequest,
  LoginRequest,
  ServiceResponse,
} from './types'

export const apiServices = {
  getAllServices: (): Promise<ServiceResponse[]> =>
    axiosInstance.get('/services').then((r) => r.data.content ?? r.data),

  getServiceById: (id: number): Promise<ServiceResponse> =>
    axiosInstance.get(`/services/${id}`).then((r) => r.data),

  createBooking: (data: CreateBookingRequest): Promise<BookingResponse> =>
    axiosInstance.post('/bookings', data).then((r) => r.data),

  trackBooking: (params: {
    bookingReference?: string
    phoneNumber?: string
  }): Promise<BookingResponse | BookingResponse[]> =>
    axiosInstance.get('/bookings/track', { params }).then((r) => r.data),

  login: (data: LoginRequest): Promise<AuthResponse> =>
    axiosInstance.post('/auth/login', data).then((r) => r.data),
}
