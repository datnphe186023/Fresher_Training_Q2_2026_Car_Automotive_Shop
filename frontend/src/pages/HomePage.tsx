import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Card, Col, Row, Spin, Alert, Modal, Typography, Button } from 'antd'
import { apiServices } from '../api/services'
import type { ServiceResponse } from '../api/types'

const { Title, Text } = Typography

const HomePage: React.FC = () => {
  const [services, setServices] = useState<ServiceResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selected, setSelected] = useState<ServiceResponse | null>(null)

  useEffect(() => {
    apiServices.getAllServices()
      .then(setServices)
      .catch(() => setError('Unable to load services. Please try again.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow px-6 py-4 flex justify-between items-center">
        <Title level={4} className="!mb-0">Car Enhancement Shop</Title>
        <div className="flex gap-4">
          <Link to="/booking"><Button type="primary">Book a Service</Button></Link>
          <Link to="/tracking"><Button>Track Order</Button></Link>
        </div>
      </nav>

      <div className="max-w-6xl mx-auto p-6">
        <Title level={2} className="mb-6">Our Services</Title>

        {loading && <div className="flex justify-center py-20"><Spin size="large" /></div>}
        {error && <Alert message={error} type="error" showIcon />}

        {!loading && !error && (
          <Row gutter={[16, 16]}>
            {services.map((service) => (
              <Col key={service.id} xs={24} sm={12} md={8} lg={6}>
                <Card
                  hoverable
                  onClick={() => setSelected(service)}
                  title={service.name}
                  extra={<Text type="secondary">{service.category?.name}</Text>}
                >
                  <p className="text-lg font-semibold text-blue-600">
                    {service.basePrice?.toLocaleString('vi-VN')} ₫
                  </p>
                  <p className="text-gray-500">{service.durationMinutes} minutes</p>
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </div>

      <Modal
        open={!!selected}
        title={selected?.name}
        onCancel={() => setSelected(null)}
        footer={[
          <Link key="book" to="/booking">
            <Button type="primary">Book This Service</Button>
          </Link>,
          <Button key="close" onClick={() => setSelected(null)}>Close</Button>,
        ]}
      >
        <p><strong>Category:</strong> {selected?.category?.name}</p>
        <p><strong>Price:</strong> {selected?.basePrice?.toLocaleString('vi-VN')} ₫</p>
        <p><strong>Duration:</strong> {selected?.durationMinutes} minutes</p>
        <p className="mt-2">{selected?.description}</p>
      </Modal>
    </div>
  )
}

export default HomePage
