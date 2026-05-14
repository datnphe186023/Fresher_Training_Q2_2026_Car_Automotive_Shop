import { useNavigate } from 'react-router-dom'
import { Layout, Menu, Button, Typography, Avatar } from 'antd'
import {
  AppstoreOutlined,
  CalendarOutlined,
  UserOutlined,
  CarOutlined,
  BarChartOutlined,
  LogoutOutlined,
} from '@ant-design/icons'
import { TOKEN_KEY } from '../api/axiosInstance'

const { Header, Sider, Content } = Layout
const { Title, Text } = Typography

function getUsernameFromToken(): string {
  try {
    const token = localStorage.getItem(TOKEN_KEY)
    if (!token) return 'Admin'
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.sub ?? payload.username ?? 'Admin'
  } catch {
    return 'Admin'
  }
}

const AdminDashboard = () => {
  const navigate = useNavigate()
  const username = getUsernameFromToken()

  const handleLogout = () => {
    localStorage.removeItem(TOKEN_KEY)
    navigate('/login')
  }

  const menuItems = [
    { key: 'services', icon: <AppstoreOutlined />, label: 'Services' },
    { key: 'bookings', icon: <CalendarOutlined />, label: 'Bookings' },
    { key: 'customers', icon: <UserOutlined />, label: 'Customers' },
    { key: 'vehicles', icon: <CarOutlined />, label: 'Vehicles' },
    { key: 'reports', icon: <BarChartOutlined />, label: 'Reports' },
  ]

  return (
    <Layout className="min-h-screen">
      <Sider theme="dark" width={220}>
        <div className="p-4 text-white text-center font-bold text-lg border-b border-gray-600">
          Car Shop Admin
        </div>
        <Menu theme="dark" mode="inline" defaultSelectedKeys={['services']} items={menuItems} />
      </Sider>

      <Layout>
        <Header className="bg-white flex items-center justify-between px-6 shadow">
          <Title level={4} className="!mb-0">Dashboard</Title>
          <div className="flex items-center gap-3">
            <Avatar icon={<UserOutlined />} />
            <Text>Welcome, <strong>{username}</strong></Text>
            <Button
              icon={<LogoutOutlined />}
              onClick={handleLogout}
              danger
            >
              Logout
            </Button>
          </div>
        </Header>

        <Content className="p-6 bg-gray-50">
          <div className="bg-white rounded-lg p-8 text-center shadow">
            <Title level={3}>Welcome to the Admin Dashboard</Title>
            <Text type="secondary">
              Select a section from the sidebar to manage your shop.
              More features will be added in upcoming weeks.
            </Text>
          </div>
        </Content>
      </Layout>
    </Layout>
  )
}

export default AdminDashboard
