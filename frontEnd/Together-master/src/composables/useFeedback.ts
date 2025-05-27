// 📍 src/composables/useFeedback.ts
import axios from 'axios'

export function useFeedback() {
  const createFeedback = async (payload: {
    projectId: number
    page: string
    x: number
    y: number
    text: string
  }) => {
    const res = await axios.post('/feedbacks/create', payload)
    return res.data
  }

  const getFeedbacksByPage = async (page: string) => {
    const res = await axios.get('/feedbacks/project', {
      params: { page },
    })
    return res.data // FeedbackDto[]
  }

  const markFeedbackAsRead = async (feedbackId: number) => {
    await axios.post(`/feedbacks/${feedbackId}/read`)
  }

  const deleteFeedback = async (feedbackId: number) => {
    await axios.delete(`/feedbacks/${feedbackId}`)
  }

  return {
    createFeedback,
    getFeedbacksByPage,
    markFeedbackAsRead,
    deleteFeedback,
  }
}
