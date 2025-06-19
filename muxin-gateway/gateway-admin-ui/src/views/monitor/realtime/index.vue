<template>
  <div class="realtime-monitor">
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card>
          <el-statistic
            title="实时QPS"
            :value="stats?.metrics.requestsPerSecond || 0"
            suffix="次/秒"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <el-statistic
            title="总请求数"
            :value="stats?.metrics.totalRequests || 0"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <el-statistic
            title="平均响应时间"
            :value="stats?.metrics.avgResponseTime || 0"
            suffix="ms"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <el-statistic
            title="错误率"
            :value="stats?.metrics.errorRate || 0"
            suffix="%"
            :precision="2"
          />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="charts-container">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>请求趋势</span>
          </template>
          <div ref="trendChart" style="height: 400px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>状态码分布</span>
          </template>
          <div ref="statusChart" style="height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { useMonitorStore } from '@/stores/monitor'
import { storeToRefs } from 'pinia'

const monitorStore = useMonitorStore()
const { realtimeStats: stats } = storeToRefs(monitorStore)

const trendChart = ref<HTMLElement>()
const statusChart = ref<HTMLElement>()

let trendChartInstance: echarts.ECharts | null = null
let statusChartInstance: echarts.ECharts | null = null

// 初始化图表
const initCharts = () => {
  // 趋势图
  trendChartInstance = echarts.init(trendChart.value!)
  trendChartInstance.setOption({
    title: { text: '' },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'time',
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      name: 'QPS'
    },
    series: [{
      name: 'QPS',
      type: 'line',
      smooth: true,
      symbol: 'none',
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
          { offset: 1, color: 'rgba(24, 144, 255, 0)' }
        ])
      },
      lineStyle: {
        color: '#1890ff',
        width: 2
      },
      data: []
    }]
  })
  
  // 状态码分布图
  statusChartInstance = echarts.init(statusChart.value!)
  statusChartInstance.setOption({
    title: { text: '' },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10
    },
    series: [{
      name: '状态码',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 20,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: []
    }]
  })
}

// 更新图表数据
const updateCharts = () => {
  if (!stats.value) return
  
  // 更新趋势图
  if (trendChartInstance) {
    const option = trendChartInstance.getOption() as any
    const data = option.series[0].data
    data.push({
      name: new Date().toString(),
      value: [new Date(), stats.value.metrics.requestsPerSecond]
    })
    
    // 保留最近100个点
    if (data.length > 100) {
      data.shift()
    }
    
    trendChartInstance.setOption(option)
  }
  
  // 更新状态码分布
  if (statusChartInstance) {
    const distribution = stats.value.statusCodeDistribution
    statusChartInstance.setOption({
      series: [{
        data: [
          { value: distribution['2xx'], name: '2xx 成功', itemStyle: { color: '#52c41a' } },
          { value: distribution['3xx'], name: '3xx 重定向', itemStyle: { color: '#1890ff' } },
          { value: distribution['4xx'], name: '4xx 客户端错误', itemStyle: { color: '#faad14' } },
          { value: distribution['5xx'], name: '5xx 服务器错误', itemStyle: { color: '#f5222d' } }
        ]
      }]
    })
  }
}

// 监听数据变化
watch(stats, () => {
  updateCharts()
})

onMounted(() => {
  initCharts()
  monitorStore.connectWebSocket()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    trendChartInstance?.resize()
    statusChartInstance?.resize()
  })
})

onUnmounted(() => {
  monitorStore.disconnectWebSocket()
  trendChartInstance?.dispose()
  statusChartInstance?.dispose()
})
</script>

<style lang="scss" scoped>
.realtime-monitor {
  .stats-cards {
    margin-bottom: 20px;
    
    .el-card {
      :deep(.el-card__body) {
        padding: 20px;
      }
    }
  }
  
  .charts-container {
    .el-card {
      :deep(.el-card__body) {
        padding: 10px;
      }
    }
  }
}
</style>