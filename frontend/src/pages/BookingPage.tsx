import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Form, Input, Select, DatePicker, Button, Alert, Card, Typography } from 'antd'
import { apiServices } from '../api/services'
import type { ServiceResponse } from '../api/types'

const { Title } = Typography

const BookingPage: React.FC = () => {
  const [services, setServices] = useState<ServiceResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [bookingRef, setBookingRef] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    apiServices.getAllServices().then(setServices).catch(() => {})
  }, [])

  const onFinish = async (values: {
    phoneNumber: string
    name?: string
    email?: string
    serviceId: number
    bookingDate: { toISOString: () => string }
  }) => {
    setLoading(true)
    setError(null)
    try {
      const response = await apiServices.createBooking({
        phoneNumber: values.phoneNumber,
        name: values.name,
        email: values.email,
        serviceId: values.serviceId,
        bookingDate: values.bookingDate.toISOString(),
      })
      const res = response as { bookingReference?: string }
      setBookingRef(res.bookingReference ?? '')
      form.resetFields()
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(msg ?? 'Failed to create booking. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow px-6 py-4 flex justify-between items-center">
        <Link to="/" className="text-blue-600 font-semibold">← Back to Services</Link>
        <Link to="/tracking"><Button>Track Order</Button></Link>
      </nav>

      <div className="max-w-lg mx-auto p-6">
        <Card>
          <Title level={3}>Book a Service</Title>

          {bookingRef && (
            <Alert
              type="success"
              className="mb-4"
              message="Booking Confirmed!"
              description={`Your booking reference is: ${bookingRef}. Use this to track your order.`}
              showIcon
            />
          )}
          {error && <Alert message={error} type="error" className="mb-4" showIcon />}

          <Form form={form} layout="vertical" onFinish={onFinish}>
            <Form.Item
              label="Phone Number"
              name="phoneNumber"
              rules={[{ required: true, message: 'Phone number is required' }]}
            >
              <Input placeholder="e.g. 0901234567" />
            </Form.Item>
            <Form.Item label="Name" name="name">
              <Input placeholder="Optional" />
            </Form.Item>
            <Form.Item
              label="Email"
              name="email"
              rules={[{ type: 'email', message: 'Invalid email format' }]}
            >
              <Input placeholder="Optional — for confirmation" />
            </Form.Item>
            <Form.Item
              label="Service"
              name="serviceId"
              rules={[{ required: true, message: 'Please select a service' }]}
            >
              <Select placeholder="Select a service">
                {services.map((s) => (
                  <Select.Option key={s.id} value={s.id}>
                    {s.name} — {s.basePrice?.toLocaleString('vi-VN')} ₫
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item
              label="Booking Date"
              name="bookingDate"
              rules={[{ required: true, message: 'Please select a date' }]}
            >
              <DatePicker showTime className="w-full" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block>
                Confirm Booking
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </div>
    </div>
  )
}

export default BookingPage
