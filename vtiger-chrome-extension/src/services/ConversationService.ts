import ConfigLoader from "../config/ConfigLoader";
import type { ApiSuccessResponse } from "../http/ApiSuccessResponse"
import axiosClient from "../http/axiosClient";

export type WhatsAppConversationMessage = {
    direction: string;
    author: string;
    date: string;
    messageText: string;
}

export type WhatsAppConversationFullInfo = {
    caseId: string;
    caseNo: string;
    messages: WhatsAppConversationMessage[];
}

const getConversationForCase = (caseId: string): Promise<ApiSuccessResponse<WhatsAppConversationFullInfo>> => {
    return axiosClient.get(`${ConfigLoader.getConfig().apiBaseUrl}/vtiger/conversations/case/${caseId}`);
}

export default {
    getConversationForCase
}