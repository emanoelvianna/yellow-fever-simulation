(function() {

  var gulp = require('gulp');
  var uglify = require('gulp-uglify');
  var concat = require('gulp-concat');

  gulp.task('build', function() {
    gulp.src('app/**/*.js')
      .pipe(concat('standalone-chart.js'))
      .pipe(uglify())
      .pipe(gulp.dest('dist'));
  });

}());
