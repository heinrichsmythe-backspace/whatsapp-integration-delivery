<script lang="ts" setup>
defineProps({
  loading: {
    type: Boolean,
    requried: true,
  },
});
</script>
<template>
  <div class="">
  <div class="spinner-border text-primary" role="status" v-if="loading">
    <span class="visually-hidden"></span>
  </div>
  </div>
</template>
