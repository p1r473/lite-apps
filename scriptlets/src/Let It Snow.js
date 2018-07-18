(function() {

/**
 * A timer class used to measure differences in two times or count down to completion.
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
  const canvas = document.createElement("canvas");
  canvas.style.position = "fixed";
  canvas.style.left = 0;
  canvas.style.top = 0;
  canvas.style.width = "100vw";
  canvas.style.height = "100vh";
  canvas.style.zIndex = "100000";
  canvas.style.pointerEvents = "none";
  document.body.insertBefore(canvas, document.body.children[0]);

  const ctx = canvas.getContext("2d");

  const num_flakes = 300,
      wind_change_speed = 0.0005,
      fall_speed_coefficient = 50,
      wind_speed = 15
      scale_coefficient_1 = 3;
      scale_min = 0.8
      scale_coefficient = 1,
      screen_max_overlap = 20,
      min_fall_speed = 65;
  let screen_width = canvas.width = window.innerWidth,
    screen_height = canvas.height = window.innerHeight;
    v_overlap_threshhold = screen_height + screen_max_overlap;
    h_overlap_threshhold = screen_width + screen_max_overlap;

  // Creating one snowflake image and re-using it is MUCH faster to render
  // This is the raidus a snowlfake can have
  const max_snowflake_size = (scale_coefficient_1 + scale_min) * scale_coefficient;
  /** Misleading variable name. This is the max diameter of the snowflakes *when drawn to the screen* it's actually double this in the background canvas */
  const snowflake_diameter = max_snowflake_size * 2;
  const snowflake_canvas_dimensions = snowflake_diameter * 2;
  const snowflake_canvas = flake_canvas = document.createElement("canvas")
  const sCtx = snowflake_canvas.getContext("2d");
  const flakeGradient = sCtx.createRadialGradient(snowflake_diameter, snowflake_diameter, 0, snowflake_diameter, snowflake_diameter, snowflake_diameter);
  flakeGradient.addColorStop(0, 'hsla(255,255%,255%,1)');
  flakeGradient.addColorStop(1, 'hsla(255,255%,255%,0)');
  sCtx.fillStyle = flakeGradient;
  sCtx.fillRect(0, 0, snowflake_canvas_dimensions, snowflake_canvas_dimensions);


  let dtTimer = new Timer(0, true);
  var snowflakes = [];

  let wind_timer = new Timer(0, true);
  for (var i = 0; i < num_flakes; ++i) {
    const flake = new Flake();
    flake.y = Math.random() * (screen_height + screen_max_overlap);
    flake.x = Math.random() * screen_width;
    flake.sz = (Math.random() * scale_coefficient_1 + scale_min) * scale_coefficient;
    flake.dy = (Math.pow(flake.sz, 2.5) * .1) * fall_speed_coefficient * (Math.random() * 2 + 1);
    flake.dy = flake.dy < min_fall_speed ? min_fall_speed : flake.dy;

    flake.final_scale_coefficient = flake.sz / snowflake_diameter;

    flake.dy_squared = flake.dy * flake.dy;

    flake.wind_offset = Math.random() * Math.PI / 1.3;

    flake.dx_coefficient = flake.sz * wind_speed;

    flake.dx = 0;
    flake.speed = 0;

    snowflakes.push(flake);
  }
  function frame() {
    ctx.clearRect(0, 0, screen_width, screen_height);
    //ctx.fillStyle = 'hsla(242, 95%, 3%, 1)';
    //ctx.fillRect(0, 0, screen_width, screen_height);
    ctx.fill();
    window.requestAnimationFrame(frame);
    const dt = dtTimer.elapsed_time * (1 / 1000);
    dtTimer.reset();
    const wind_state = wind_timer.elapsed_time * wind_change_speed;
    for (var i = 0; i < snowflakes.length; ++i) {
      const flake = snowflakes[i];
      flake.dx = Math.sin(wind_state + flake.wind_offset) * flake.dx_coefficient;
      flake.speed = Math.sqrt(flake.dx * flake.dx + flake.dy_squared);
      flake.y += flake.dy * dt;
      flake.x += flake.dx * dt;
      if (flake.y > v_overlap_threshhold) flake.y = -screen_max_overlap;
      if (flake.x > h_overlap_threshhold) flake.x = -screen_max_overlap;
      if (flake.x < -screen_max_overlap) flake.x = h_overlap_threshhold;
      flake.draw();
    }
  }
  function Flake() {
    this.draw = function () {
      const angle = Math.atan(this.dx / this.dy);

      ctx.save();
      ctx.translate(this.x, this.y);
      ctx.rotate(-angle);
      ctx.scale(this.final_scale_coefficient, this.final_scale_coefficient * Math.max(1, Math.pow(this.speed, 0.7) / 15))
      ctx.drawImage(snowflake_canvas, -snowflake_canvas_dimensions / 2, -snowflake_canvas_dimensions / 2)
      ctx.restore();
    }
  }

  function scrambleFlakes() {
    for (var i = 0; i < num_flakes; ++i) {
      snowflakes[i].y = Math.random() * (screen_height + screen_max_overlap);
      snowflakes[i].x = Math.random() * screen_width;
    }
  }

  scrambleFlakes();

  function onResizeOrVisibilityChange() {
    canvas.width = screen_width = window.innerWidth;
    canvas.height = screen_height = window.innerHeight;
    v_overlap_threshhold = screen_height + screen_max_overlap;
    h_overlap_threshhold = screen_width + screen_max_overlap;

    scrambleFlakes();
  }

  document.addEventListener("visibilitychange", () => setTimeout(onResizeOrVisibilityChange, 100), false);
  window.addEventListener('resize', onResizeOrVisibilityChange, false);

  frame();
})();