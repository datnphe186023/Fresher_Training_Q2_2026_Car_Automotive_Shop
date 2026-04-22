import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Input, Button, List, Tag, Alert, Typography, Card } from 'antd'
import { apiServices } from '../api/services'
import type { BookingResponse } from '../api/types'

const { Title } = Typography

const STATUS_COLORS: Record<string, string> = {
  PENDING: 'default',
  CONFIRMED: 'blue',
  IN_PROGRESS: 'orange',
  COMPLETED: 'green',
  CANCELLED: 'red',
}

const TrackingPage: React.FC = () => {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<BookingResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [searched, setSearched] = useState(false)
  const [validationError, setValidationError] = useState<string | null>(null)

  const handleSearch = async () => {
    if (!query.trim()) {
      setValidationError('Please enter a booking reference or phone number')
      return
    }
    setValidationError(null)
    setLoading(true)
    setSearched(false)
    try {
      // Detect if it looks like a phone number (digits/+/spaces) or a booking reference
      const isPhone = /^[+\d\s\-()]+$/.test(query.trim()) && query.replace(/\D/g, '').length >= 10
      const params = isPhone
        ? { phoneNumber: query.trim() }
        : { bookingReference: query.trim() }

      const data = await apiServices.trackBooking(params)
      setResults(Array.isArray(data) ? data : [data])
    } catch {
      setResults([])
    } finally {
      setLoading(false)
      setSearched(true)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow px-6 py-4 flex justify-between items-center">
        <Link to="/" className="text-blue-600 font-semibold">← Back to Services</Link>
        <Link to="/booking"><Button type="primary">Book a Service</Button></Link>
      </nav>

      <div className="max-w-2xl mx-auto p-6">
        <Card>
          <Title level={3}>Track Your Order</Title>
          <div className="flex gap-2 mb-4">
            <Input
              placeholder="Enter booking reference or phone number"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onPressEnter={handleSearch}
              size="large"
            />
            <Button type="primary" size="large" loading={loading} onClick={handleSearch}>
              Search
            </Button>
          </div>

          {validationError && (
            <Alert message={validationError} type="warning" showIcon className="mb-4" />
          )}

          {searched && results.length === 0 && (
            <Alert
              message="No bookings found for the provided information"
              type="info"
              showIcon
            />
          )}

          {results.length > 0 && (
            <List
              dataSource={results}
              renderItem={(booking) => (
                <List.Item key={booking.id}>
                  <List.Item.Meta
                    title={
                      <span className="font-mono font-semibold">{booking.bookingReference}</span>
                    }
                    description={
                      <>
                        <p>{booking.service?.name}</p>
                        <p className="text-gray-500">
                          {new Date(booking.bookingDate).toLocaleString('vi-VN')}
                        </p>
                      </>
                    }
                  />
                  <Tag color={STATUS_COLORS[booking.status] ?? 'default'}>
                    {booking.status}
                  </Tag>
                </List.Item>
              )}
            />
          )}
        </Card>
      </div>
    </div>
  )
}

export default TrackingPage
