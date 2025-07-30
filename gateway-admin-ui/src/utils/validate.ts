// 验证用户名
export function validateUsername(username: string): boolean {
  const reg = /^[a-zA-Z0-9_]{4,20}$/
  return reg.test(username)
}

// 验证密码强度
export function getPasswordStrength(password: string): 'weak' | 'medium' | 'strong' {
  let strength = 0
  if (password.length >= 8) strength++
  if (password.length >= 12) strength++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++
  if (/\d/.test(password)) strength++
  if (/[!@#$%^&*]/.test(password)) strength++
  
  if (strength <= 2) return 'weak'
  if (strength <= 4) return 'medium'
  return 'strong'
}

// 验证邮箱
export function validateEmail(email: string): boolean {
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return reg.test(email)
}

// 验证手机号
export function validatePhone(phone: string): boolean {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

// 验证IP地址
export function validateIP(ip: string): boolean {
  const reg = /^(\d{1,3}\.){3}\d{1,3}$/
  if (!reg.test(ip)) return false
  
  const parts = ip.split('.')
  return parts.every(part => {
    const num = parseInt(part)
    return num >= 0 && num <= 255
  })
}

// 验证URL
export function validateURL(url: string): boolean {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
} 