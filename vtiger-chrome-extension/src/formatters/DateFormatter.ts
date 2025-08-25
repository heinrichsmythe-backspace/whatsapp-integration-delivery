import DateUtils from "../utils/DateUtils";
import dayjs from "dayjs"
import durationPlugin from 'dayjs/plugin/duration';
import relativeTimePlugin from 'dayjs/plugin/relativeTime';

dayjs.extend(durationPlugin)
dayjs.extend(relativeTimePlugin)

export type TimeObject = {
    HH: string;
    mm: string; 
}

const formatDate = (date: Date | string): string => {
    if(!date){
        return 'Invalid date';
    }
    return dayjs(date).format('YYYY-MM-DD');
}

const formatDateTime = (date: Date | string): string => {
    if(!date){
        return 'Invalid date';
    }
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss');
}

const formatDuration = (seconds: number): string => {
    return dayjs.duration(seconds, 'seconds').format('HH:mm:ss');
}

const formatElapsedTime = (ms: string | number): string => {
    if(Number.isNaN(ms)){
        return 'error';
    }
    return dayjs.duration(+ms, 'milliseconds').format('D[d] H[h] m[m] s[s]');
}

const timeAgo = (date: Date | string): string => {
    const dateAgo = DateUtils.stringToDate(date);
    if(!dateAgo){
        return 'error';
    }
    return dayjs(dateAgo).fromNow();
}

const formatStringToTime = (timeString: string): TimeObject => {
    const timeStringSplit = timeString.split(':');
    if(timeStringSplit.length != 2){
        return {
            HH: '',
            mm: ''
        }
    }
    return {
        HH: timeStringSplit[0],
        mm: timeStringSplit[1]
    }
}

const formatTimeToString = (timeSelect: TimeObject): string => {
    return timeSelect.HH + ':' + timeSelect.mm;
}

export default {
    formatDate,
    formatDateTime,
    formatDuration,
    formatElapsedTime,
    timeAgo,
    formatStringToTime,
    formatTimeToString,
}