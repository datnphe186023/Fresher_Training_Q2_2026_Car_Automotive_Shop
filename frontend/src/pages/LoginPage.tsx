import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Form, Input, Alert, Card, Typography } from 'antd'
import { apiServices } from '../api/services'
import { TOKEN_KEY } from '../api/axiosInstance'

const { Title } = Typography

const LoginPage: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true)
    setError(null)
    try {
      const response = await apiServices.login(values)
      localStorage.setItem(TOKEN_KEY, response.accessToken)
      navigate('/admin')
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status
      if (status === 401) {
        setError('Invalid username or password')
      } else {
        setError('An error occurred. Please try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <Card className="w-full max-w-sm shadow-md">
        <Title level={3} className="text-center mb-6">Admin Login</Title>
        {error && <Alert message={error} type="error" className="mb-4" showIcon />}
        <Form layout="vertical" onFinish={onFinish} autoComplete="off">
          <Form.Item
            label="Username"
            name="username"
            rules={[{ required: true, message: 'Please enter your username' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            label="Password"
            name="password"
            rules={[{ required: true, message: 'Please enter your password' }]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              Login
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default LoginPage
