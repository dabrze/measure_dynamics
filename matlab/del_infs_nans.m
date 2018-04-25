function retval = del_infs_nans(X)
  retval = X;
  retval(any(isnan(retval) | isinf(retval), 2), :) = [];
  return;