(function() {
  /**
   * Basic timer/stopwatch class
   * @author Rock
     */
  class Timer {
    /**
     * @param {number} duration The duration of the timer. Not really needed if using as a stopwatch
     * @param {boolean} auto_start
     */
    constructor(duration, auto_start = true) {
      /**
       * Technically this is the "start time" but it's really just used for the offset to get `time_running` and `time_remaining` because pausing will cause this to change
       */
      this._start_time = Date.now();
      this._paused = true;
      this._paused_at = Date.now();

      this.duration = duration;

      if(auto_start) this.start();
    }

    /** Returns the current elapsed time of the timer */
    get elapsed_time() {
      if(!this.paused) return Date.now() - this._start_time;
      return this._paused_at - this._start_time;
    }

    /** Returns the time remaining */
    get remaining_time() {
      return Math.max(0, this.duration - this.elapsed_time);
    }

    /** Retruns whether the timer is paused or not */
    get paused() {
      return this._paused;
    }

    /** Returns whether the timer has completed (reached 0ms) */
    get is_complete() {
      return this.elapsed_time >= this.duration;
    }

    /**
     * Returns a promise that resolves once the timer has completed.
     *
     * **EVEN IF THE TIMER IS EDITED, THIS WILL STILL RESOLVE AT THE SAME TIME**
     */
    get completion() {
      return module.exports.wait(this.remaining_time);
    }

    /** Starts or resumes the timer. */
    start() {
      this._start_time = Date.now() - this.elapsed_time;
      this._paused = false;
      return this;
    }

    /** Resets the timer without pausing */
    reset() {
      this._paused_at = this._start_time = Date.now();
      return this;
    }

    /** Pauses the timer without resetting it */
    pause() {
      this._paused = true;
      this._paused_at = Date.now();
      return this;
    }

    /** Stops and resets the timer */
    stop() {
      this._paused = true;
      return this;
    }
  }
  /** @type {HTMLCanvasElement} */
  var canvas = document.createElement("canvas");
  canvas.style.position = "fixed";
  canvas.style.left = 0;
  canvas.style.top = 0;
  canvas.style.position = "fixed";
  canvas.style.width = "100vw";
  canvas.style.height = "100vh";
  canvas.style.zIndex = "100000";
  canvas.style.pointerEvents = "none";
  document.body.insertBefore(canvas, document.body.children[0]);

    ctx = canvas.getContext("2d");
  let screen_width = canvas.width = window.innerWidth,
    screen_height = canvas.height = window.innerHeight;

  let dtTimer = new Timer(0, true);
  let running = false;
  let scramble;

  (function() {
    var snowflakes = [];

    const num_flakes = 200,
      wind_change_speed = 0.0005,
      fall_speed_coefficient = 50,
      wind_speed = 0.2;
      scale_coefficient = 1,
      screen_max_overlap = 20,
      min_fall_speed = 65;

    let wind_timer = new Timer(0, true);
    running = true;
    for (var i = 0; i < num_flakes; ++i) {
      const flake = new Flake();
      flake.y = Math.random() * (screen_height + 50);
      flake.x = Math.random() * screen_width;
      flake.t = Math.random() * (Math.PI * 2);
      flake.sz = (Math.random() * 3 + 0.8) * scale_coefficient;
      flake.dy = (Math.pow(flake.sz, 2.5) * .1) * fall_speed_coefficient * (Math.random() * 2 + 1);
      flake.dy = flake.dy < min_fall_speed ? min_fall_speed : flake.dy;

      flake.wind_offset = Math.random() * Math.PI / 1.3;

      flake.dx = 0;
      flake.speed = 0;

      snowflakes.push(flake);
    }
    go();
    function go() {
      ctx.clearRect(0, 0, screen_width, screen_height);
      ctx.fill();
      if(!running) {
        snowflakes = [];
        return;
      }
      window.requestAnimationFrame(go);
      const dt = dtTimer.elapsed_time * (1 / 1000);
      dtTimer.reset();
      for (var i = 0; i < snowflakes.length; ++i) {
        const flake = snowflakes[i];
        flake.dx = Math.sin(wind_timer.elapsed_time * wind_change_speed + flake.wind_offset) * (flake.sz * wind_speed) * 80;
        flake.speed = Math.sqrt(flake.dx * flake.dx + flake.dy * flake.dy);
        flake.y += flake.dy * dt;
        flake.x += flake.dx * dt;
        if (flake.y > screen_height + 50) flake.y = -10 - Math.random() * screen_max_overlap;
        if (flake.x > screen_width + screen_max_overlap) flake.x = - screen_max_overlap;
        if (flake.x < - screen_max_overlap) flake.x = screen_width + screen_max_overlap;
        flake.draw();
      }
    }
    function Flake() {
      this.draw = function () {
        ctx.save();
        ctx.translate(this.x, this.y);

        const angle = Math.atan(this.dx / this.dy);
        ctx.rotate(-angle);

        ctx.scale(1, Math.max(1, Math.pow(this.speed, 0.7) / 15))

        this.g = ctx.createRadialGradient(0, 0, 0, 0, 0, this.sz);
        this.g.addColorStop(0, 'hsla(255,255%,255%,1)');
        this.g.addColorStop(1, 'hsla(255,255%,255%,0)');
        ctx.moveTo(0, 0);
        ctx.fillStyle = this.g;
        ctx.beginPath();
        ctx.arc(0, 0, this.sz, 0, Math.PI * 2, true);
        ctx.closePath();
        ctx.fill();
        ctx.restore();
      }
    }

    scramble = () => {
      for (var i = 0; i < num_flakes; ++i) {
        snowflakes[i].y = Math.random() * (screen_height + 50);
        snowflakes[i].x = Math.random() * screen_width;
      }
    }

    scramble();
  })();
  function onResizeOrVisibilityChange() {
    canvas.width = screen_width = window.innerWidth;
    canvas.height = screen_height = window.innerHeight;

    if(running) scramble();
  }
  document.addEventListener("visibilitychange", () => setTimeout(onResizeOrVisibilityChange, 100), false);
  window.addEventListener('resize', onResizeOrVisibilityChange, false);
})();