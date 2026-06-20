<script>
</script>
<template>
  <section class="about-me">
    <div class="about-content">
      <p class="intro">
        Here is <strong>AncauqL</strong>.
      </p>

      <p class="pronunciation">
        Pronounced: <strong>/ˈæn.kɔː.kjuː.el/</strong>
      </p>

      <p class="slogan">
        <em>Less tether, more ether.</em>
      </p>
    </div>
  </section>
</template>

<style scoped>
.about-me {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  color: #1f1e33;
}

.about-content {
  max-width: 720px;
  width: 100%;
  text-align: center;
  font-family: "Times New Roman", Georgia, "Noto Serif", serif;
}

.intro {
  margin: 0;
  font-size: 36px;
  line-height: 1.45;
}

.pronunciation {
  margin: 16px 0 0;
  font-size: 22px;
  line-height: 1.7;
}

.slogan {
  margin: 18px 0 0;
  font-size: 26px;
  line-height: 1.65;
  color: #545c64;
}

.intro strong,
.pronunciation strong {
  font-weight: 700;
}
</style>
