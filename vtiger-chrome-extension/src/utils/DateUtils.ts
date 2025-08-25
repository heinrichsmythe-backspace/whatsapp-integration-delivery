import dayjs from "dayjs"

const addDays = (date: Date, days: number): Date => {
    return dayjs(date).add(days, 'day').toDate();
}

const stringToDate = (dateStr?: string | Date): Date | undefined => {
    if(!dateStr){
        return undefined;
    }
    return dayjs(dateStr).toDate();
}

export default {
    addDays,
    stringToDate
}